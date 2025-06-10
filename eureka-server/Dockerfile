# Gradle build
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .

RUN chmod +x ./gradlew && ./gradlew build --no-daemon

# 2단계: 빌드된 jar 파일만 가져와서 실행용 이미지 생성
FROM eclipse-temurin:17-jre-focal
WORKDIR /app
# Gradle의 빌드 결과물 경로에서 jar파일 복사
COPY --from=build /app/eureka-server/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
