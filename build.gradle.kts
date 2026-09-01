import com.google.protobuf.gradle.*


plugins {
	java
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.google.protobuf") version "0.9.5"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

val grpcVersion = "1.62.2"
val protobufVersion = "4.28.2"


dependencies {
	// Spring Boot 기본 구성
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	runtimeOnly("com.mysql:mysql-connector-j")
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// Redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-cache:3.4.3")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
	implementation("org.redisson:redisson-spring-boot-starter:3.31.0")

	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// gRPC + Protobuf
	implementation("com.google.protobuf:protobuf-java:$protobufVersion")
	implementation("io.grpc:grpc-stub:$grpcVersion")
	implementation("io.grpc:grpc-protobuf:$grpcVersion")
	implementation("io.grpc:grpc-netty-shaded:$grpcVersion")

	// Java 9 이상 지원용 주석 API (Generated 어노테이션 문제 해결용)
	implementation("javax.annotation:javax.annotation-api:1.3.2")

	// 테스트
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")


	// S3
	implementation ("software.amazon.awssdk:s3:2.25.12")

	// h2 db
	testImplementation ("com.h2database:h2")

	// Kafka
	implementation("org.apache.kafka:kafka-clients")
	implementation("org.springframework.kafka:spring-kafka")

	// Prometheus
	implementation ("org.springframework.boot:spring-boot-starter-actuator")
	implementation ("io.micrometer:micrometer-registry-prometheus")

	// Resilience4j Circuit Breaker
	implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
	implementation("org.springframework.boot:spring-boot-starter-aop")

}

protobuf {
	protoc {
		artifact = "com.google.protobuf:protoc:$protobufVersion"
	}
	plugins {
		id("grpc") {
			artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
		}
	}
	generateProtoTasks {
		all().forEach {
			it.plugins {
				id("grpc")
			}
		}
	}
}

tasks.withType<Test> {
	systemProperty("idea.launcher.classpath.index", "true")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<JavaCompile> {
	options.compilerArgs.add("-parameters")
}


