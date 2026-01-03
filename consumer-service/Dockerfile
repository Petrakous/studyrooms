# Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY consumer-service consumer-service

RUN chmod +x mvnw && ./mvnw -pl consumer-service -am -DskipTests package

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

ARG JAR_FILE=consumer-service/target/studyrooms-consumer-0.0.1-SNAPSHOT.jar
COPY --from=builder /app/${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]
