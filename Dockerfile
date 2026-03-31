# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

COPY src/ src/

RUN ./mvnw -q -DskipTests clean package

FROM eclipse-temurin:21-jre-jammy

RUN addgroup --system caprigestor \
    && adduser --system --ingroup caprigestor caprigestor \
    && mkdir -p /app/logs

WORKDIR /app

COPY --from=build --chown=caprigestor:caprigestor /workspace/target/CapriGestor-0.0.1-SNAPSHOT.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=prod \
    CAPRIGESTOR_MESSAGING_ENABLED=false \
    JAVA_OPTS=""

EXPOSE 8080

USER caprigestor

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

