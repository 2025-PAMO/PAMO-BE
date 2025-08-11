# syntax=docker/dockerfile:1

FROM gradle:8.13-jdk17 AS build
WORKDIR /workspace
# Gradle 설정과 래퍼 먼저 복사 (캐시 활용)
COPY settings.gradle build.gradle gradle.properties* ./
COPY gradle gradle
COPY gradlew gradlew
RUN chmod +x gradlew
RUN ./gradlew --no-daemon dependencies

# 소스와 리소스 복사
COPY src src

# 테스트 제외 빌드
RUN ./gradlew clean bootJar -x test --no-daemon
RUN rm -f build/libs/*-plain.jar && mv build/libs/*.jar app.jar

## 2) Run stage
FROM eclipse-temurin:17-jre
ENV TZ=Asia/Seoul
WORKDIR /app
COPY --from=build /workspace/app.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-Duser.timezone=Asia/Seoul","-jar","/app/app.jar"]