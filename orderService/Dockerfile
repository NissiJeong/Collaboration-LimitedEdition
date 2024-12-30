# 실행 단계: Java 런타임만 필요
FROM openjdk:17-jdk-alpine

# 애플리케이션 작업 디렉터리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사 (로컬 빌드된 JAR 파일 경로를 Docker 컨테이너로 복사)
COPY build/libs/orderService-0.0.1-SNAPSHOT.jar app.jar

# 컨테이너 실행 시 JAR 파일 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.application.name=order-service"]
