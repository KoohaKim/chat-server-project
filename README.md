# 실시간 채팅 서버 프로젝트

## 프로젝트 소개

카카오톡과 같은 대규모 실시간 채팅 서비스를 클론하며, 여러 기술적 문제들을 직접 부딪혀보기 위해 시작한 프로젝트입니다.

단순히 메시지를 주고받는 채팅 앱을 만드는 것을 넘어서, 실제 서비스 규모에서 마주칠 수 있는 질문들을 스스로 던지고 답을 찾아가는 과정으로 프로젝트를 설계했습니다.

- 서버 인스턴스가 여러 대로 늘어나면, 다른 인스턴스에 접속한 유저에게는 메시지를 어떻게 전달할 것인가?
- 메시지가 계속 쌓이면 하나의 DB로는 감당하기 어려운데, 어떻게 나눠 저장하고 조회할 것인가?
- 여러 서버와 DB에 걸쳐 있어도 메시지 ID가 겹치지 않고 시간순으로 정렬되려면 어떻게 해야 하는가?
- 채팅방 목록처럼 자주 조회되는 데이터는 매번 무거운 조회를 수행하지 않고 어떻게 빠르게 응답할 수 있는가?
- 메시지 전달 도중 장애가 발생하면 어떻게 재처리하고 복구할 것인가?

이런 질문에 대해 직접 구조를 설계하고, 구현하고, 부하 테스트로 검증하며 답을 찾아가는 방식으로 프로젝트를 진행했습니다.

## 기술 스택

- **Backend**: Spring Boot, Spring WebSocket, Spring Data JPA
- **Messaging**: Apache Kafka
- **Database**: MySQL (메인 DB + 메시지 샤드 DB 2개), HikariCP
- **Cache / In-memory**: Redis, Redisson
- **인증**: JWT
- **파일 저장**: AWS S3
- **모니터링**: Prometheus + Grafana
- **부하 테스트**: k6

## 아키텍처

```text
Client (WebSocket)
        │
        ▼
┌─────────────────────┐        ┌─────────────────────┐
│  Server Instance A  │        │  Server Instance B  │
└─────────┬───────────┘        └──────────┬──────────┘
          │                               │
          ▼                               ▼
     MySQL (chatRoomId 기준 샤딩)
          │
          │ DB 커밋 이후 이벤트 발행
          ▼
   ┌──────────────┐
   │    Kafka     │  ← 인스턴스 간 메시지 릴레이
   └──────┬───────┘
          │
          ▼
각 인스턴스의 Consumer
          │
          ▼
접속 중인 세션에 WebSocket 브로드캐스트
```

- 메시지를 보낸 사용자도 다른 참여자와 동일하게 **Kafka → Consumer → WebSocket 브로드캐스트** 경로로 메시지를 받습니다.
- 각 인스턴스는 자신에게 접속한 세션을 관리합니다. 다른 인스턴스에 접속한 사용자에게는 Kafka를 통해 메시지를 전달합니다.
- 메시지 저장(MySQL) → 커밋 후 이벤트 발행 → Kafka를 통한 인스턴스 간 릴레이 → 실시간 브로드캐스트로 이어지는 구조이며, 세부 설계와 근거는 아래 핵심 설계 결정에 정리했습니다.

### DB 구성

| DB | 테이블 |
|---|---|
| `minimain` | `users`, `chat_rooms`, `chat_room_members`, `chat_room_lastmessage` |
| `minimessage0`, `minimessage1` | `messages` |

## 핵심 설계 결정

### 1. 메시지 순서 보장 — Kafka partition key
`chatRoomId`를 partition key로 사용해 같은 채팅방의 메시지가 항상 같은 파티션으로 들어가게 했습니다. 채팅방 단위로 순서가 보장됩니다. (인스턴스 간 릴레이는 초기에 gRPC로 구현했다가 Kafka로 전환했습니다.)

### 2. 장애 시 유실 방지 — Manual Ack
Consumer가 WebSocket 브로드캐스트를 끝낸 **뒤에만** `ack.acknowledge()`를 호출합니다(`manual_immediate`). 예외가 나면 ack하지 않고 예외를 던져 Spring Kafka 컨테이너의 재시도에 맡깁니다. 별도의 ErrorHandler/DLT 설정은 없어서, 기본 재시도 횟수를 넘기면 해당 레코드는 건너뜁니다. 재처리 시 일부 세션에 중복 전달될 수 있으므로 클라이언트에서 `messageId`로 중복을 걸러내는 것을 전제로 합니다. 개별 세션으로의 전송 실패는 로그만 남기고 다음 세션으로 넘어갑니다. 메시지는 이미 DB에 저장되어 있어 이후 조회로 복구할 수 있습니다.

### 3. 롤백된 메시지가 전파되지 않도록 — AfterCommit 이후 이벤트 발행
`@TransactionalEventListener(phase = AFTER_COMMIT)`와 `@Async`로 DB 커밋이 끝난 뒤에만 Kafka 이벤트를 발행합니다. 같은 이벤트를 구독하는 **별도 리스너**가 채팅방 목록 캐시를 무효화해서 발행 로직과 캐시 로직을 분리했습니다.

### 4. 샤딩 — chatRoomId 기반 라우팅
`MessageShardKeySelector`가 `chatRoomId % 2`로 shardKey(0 또는 1)를 계산 하고 이 값을 `ShardContext`(ThreadLocal)에 담습니다. `AbstractRoutingDataSource`를 상속한 `ShardingDataSource`가 이를 읽어 DB를 선택합니다. 같은 채팅방의 메시지는 항상 같은 샤드에 저장되도록(co-location) `chatRoomId`를 샤딩 기준으로 삼았고 채팅방 목록 조회 때는 lastMessage 메타에 저장된 shardKey로 샤드별로 묶어 한 번에 조회합니다.

### 5. 분산 환경의 messageId — Snowflake
`timestamp + nodeId + sequence`를 비트로 합친 Snowflake를 직접 구현했습니다. 여러 인스턴스·샤드에서도 전역 유일하고 시간순 정렬이 됩니다. 인스턴스마다 `nodeId`를 다르게 지정합니다.

### 6. 쓰기 부하 완화 — lastMessage 디바운스
채팅방의 마지막 메시지(`chat_room_last_message`)를 메시지마다 DB에 쓰지 않고, Redis/Redisson으로 3초 동안 모았다가 한 번에 반영합니다.
- `RMap`: 방별 최신 messageId 보관 (덮어쓰기로 최신값만 유지)
- `RBucket` + TTL: 방별 중복 예약 방지
- `RDelayedQueue` → `RBlockingQueue`: 지연 전달 후 백그라운드 스레드가 flush
- Redis 상태를 공유하므로 여러 인스턴스에서도 동일하게 동작합니다.

### 7. 채팅방 목록 캐시 (Redis)
- `@Cacheable("myChatRooms")`를 `getMyChatRooms(userId)`에 적용했습니다. 저장소는 `RedisCacheManager`이며 키는 userId(`myChatRooms::<userId>`), 값은 JSON, **TTL은 60초**입니다.
- 캐시 미스일 때만 메인 DB에서 채팅방·lastMessage 메타를 읽고, 메시지 본문은 shardKey별로 묶어 샤드 DB에서 한 번에 조회합니다.
- 무효화는 메시지 DB 트랜잭션이 커밋된 뒤(`AFTER_COMMIT`)와, 디바운스된 lastMessage가 DB에 반영된 직후(flush) 해당 채팅방의 모든 멤버 캐시를 evict하는 방식입니다. flush 이후에도 지우는 이유는, 메시지 커밋 시점에는 DB의 lastMessage가 아직 이전 값이라 그 사이 조회가 오래된 값을 다시 캐시할 수 있기 때문입니다.

### 8. 인증·세션 관리
- WebSocket **handshake 시점**에 JWT를 검증합니다.
- 채팅방 참여자는 Redis Set으로 관리합니다.
- `SessionManager`: 채팅방별 세션 Set을 `ConcurrentHashMap.newKeySet()`으로 관리하고, 세션 단위 `synchronized`로 동시 `sendMessage()` 호출을 막습니다. (WebSocket 생명주기 스레드와 Kafka Consumer 스레드가 같은 세션에 접근할 수 있기 때문입니다.)
## 성능 개선: 채팅방 목록 조회 (`getMyChatRooms`)

Redis 캐싱 적용 전후의 차이를 확인하기 위해 캐시 비활성화 상태와 활성화 상태에서 동일한 k6 스크립트(`stress-test.js`)를 실행해 비교했습니다.

### 테스트 결과

| 지표 | 캐싱 전 | 캐싱 후 | 개선 |
|---|---:|---:|---:|
| 평균 응답 시간 (avg) | 208.94ms | 67.89ms | **약 67.5% 감소** |
| p90 | 777.56ms | 326.76ms | **약 58.0% 감소** |
| p95 | 862.94ms | 476.05ms | **약 44.8% 감소** |
| p99 | 1.01s | 620.90ms | **약 38.5% 감소** |
| 최대 응답 시간 (max) | 1.37s | 1.80s | 증가 |
| 초당 요청 수 (RPS) | 295.06 | 368.84 | **약 25.0% 증가** |
| HTTP 요청 실패율 | 0% | 0% | 동일 |

### 결과 분석

- 캐싱 적용 후 평균 응답 시간이 약 67.5% 감소했습니다.
- p95 응답 시간은 862.94ms에서 476.05ms로 감소했습니다.
- 초당 처리량은 약 295건에서 약 369건으로 증가했습니다.
- 두 테스트 모두 HTTP 요청 실패율 0%, 체크 성공률 100%를 기록했습니다.
- 캐싱 적용 후에도 p95 400ms 미만이라는 k6 임계값은 충족하지 못했습니다.
- 최대 응답 시간은 단일 지연의 영향을 크게 받으므로 참고 지표로만 해석합니다.

### 테스트 환경 및 조건

- **환경**: MacBook Air (Apple M2) / 16GB   , 로컬 개발 환경
- **테스트 도구**: k6
- **테스트 스크립트**: `stress-test.js`
- **최대 가상 사용자**: 500 VUs
- **테스트 시간**: 약 110초
- **부하 패턴**: 0 → 50 → 100 → 300 → 500 → 0 VUs
- **캐시 TTL**: 60초
- **캐시 비활성화**: `app.cache.enabled=false`
- **캐시 활성화**: `app.cache.enabled=true`

두 테스트는 같은 k6 스크립트와 부하 설정을 사용했으며, 캐시 비활성화 여부를 바꿔 측정했습니다. 요청 수와 RPS는 응답 속도 차이에 따라 달라질 수 있습니다.

### 재현 방법

프로젝트 루트에서 애플리케이션을 실행합니다.

**1. 캐시 비활성화**

```bash
./gradlew bootRun --args='--app.cache.enabled=false'
```

다른 터미널에서 `stress-test.js`가 있는 디렉터리로 이동한 뒤 실행합니다.

```bash
k6 run \
  --summary-trend-stats="avg,p(90),p(95),p(99),max" \
  --out json=nocache-result.json \
  stress-test.js
```

**2. 캐시 활성화**

기존 애플리케이션을 종료한 뒤 다음 명령어로 다시 실행합니다.

```bash
./gradlew bootRun --args='--app.cache.enabled=true'
```

개발용 Redis의 데이터를 초기화할 경우 다음 명령어를 사용할 수 있습니다. 이 명령어는 Redis의 모든 DB에 있는 키를 삭제하므로 다른 기능의 데이터가 없는지 먼저 확인해야 합니다.

```bash
redis-cli FLUSHALL
```

테스트 전에 캐시 대상 API를 호출해 캐시를 준비하고, 캐시가 실제로 생성되었는지 확인합니다. 이후 동일한 k6 테스트를 실행합니다.

```bash
k6 run \
  --summary-trend-stats="avg,p(90),p(95),p(99),max" \
  --out json=cache-result.json \
  stress-test.js
```

`nocache-result.json`과 `cache-result.json`은 명령어를 실행한 현재 디렉터리에 저장됩니다.

## 실행 방법

### 1. 인프라 실행 (MySQL, Kafka, Redis)

```bash
docker compose up -d
```

`minimain`, `minimessage0`, `minimessage1` 데이터베이스를 생성합니다.

`src/main/java/com/example/gooha/miniproject/sql/mini.sql`의 DDL을 사용하시면 됩니다. `messages` 테이블은 두 샤드 DB에, 나머지 테이블은 `minimain`에 생성합니다.

### 2. 설정

`src/main/resources/application.yaml`에서 다음 값을 본인 환경에 맞게 설정합니다.

- DB 계정
- Redis 및 Kafka 주소
- JWT secret
- S3 버킷

### 3. 빌드 및 실행

여러 인스턴스로 실행할 경우 인스턴스별 포트와 Snowflake `nodeId`를 다르게 지정합니다.

```bash
# 인스턴스 1
java -Dserver.port=8080 -Dsnowflake.node-id=0 -jar mini.jar

# 인스턴스 2
java -Dserver.port=8081 -Dsnowflake.node-id=1 -jar mini.jar

# 인스턴스 3
java -Dserver.port=8082 -Dsnowflake.node-id=2 -jar mini.jar
```
