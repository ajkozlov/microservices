FROM gradle:jdk17-alpine AS build
WORKDIR /app
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon
COPY src ./src
RUN ./gradlew assemble --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
COPY --from=build /app/build/libs/*.jar /add/app.jar
EXPOSE 8093
CMD ["java", "-jar", "/add/app.jar"]