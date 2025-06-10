# Gradle build
FROM gradle:7.6-jdk17 as build
WORKDIR /app
COPY . .

RUN chmod +x ./gradlew && ./gradlew build --no-daemon

FROM eclipse-temurin:17-jre-focal
WORKDIR /app

COPY --from=build /app/eureka-client/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

