package com.example.gooha.miniproject.sharding;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Snowflake {
    private static final int NODE_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;

    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;

    private final long nodeId;
    private final long customEpoch;

    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;

    public Snowflake(long nodeId, long customEpoch) {
        long maxNodeId = (1L << NODE_ID_BITS) - 1; // 1023
        if (nodeId < 0 || nodeId > maxNodeId) {
            throw new IllegalArgumentException(String.format("nodeId는 0에서 %d 사이여야 합니다.", maxNodeId));
        }
        this.nodeId = nodeId;
        this.customEpoch = customEpoch;
        log.info("Snowflake ID 생성기 초기화 완료 (Node ID: {}, Epoch: {})", nodeId, customEpoch);
    }

    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis() - customEpoch;

        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("시스템 시계가 과거로 되돌아갔습니다. ID 생성이 불가능합니다.");
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) { // 1 밀리초 안에 발급할 수 있는 4,096개의 ID를 모두 소진
                // 4096개 초과 요청 시 다음 밀리초로 대기
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = currentTimestamp;

        // 비트 조립 (시간을 22칸 밀고, 노드를 12칸 밀어서 결합)
        return (currentTimestamp << (NODE_ID_BITS + SEQUENCE_BITS))
                | (nodeId << SEQUENCE_BITS)
                | sequence;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis() - customEpoch;
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis() - customEpoch;
        }
        return timestamp;
    }

    public long[] parse(long id) {
        long maskNodeId = ((1L << NODE_ID_BITS) - 1) << SEQUENCE_BITS;

        long timestamp = (id >> (NODE_ID_BITS + SEQUENCE_BITS)) + customEpoch;
        long nodeId = (id & maskNodeId) >> SEQUENCE_BITS;
        long sequence = id & MAX_SEQUENCE;

        return new long[]{timestamp, nodeId, sequence};
    }
}