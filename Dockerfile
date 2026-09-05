# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# The repository is developed on Windows as well; normalize the shell wrapper
# before executing it in this Linux build stage.
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

COPY src/ src/

RUN ./mvnw -q -DskipTests clean package

FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && addgroup --system caprigestor \
    && adduser --system --ingroup caprigestor caprigestor \
    && mkdir -p /app/logs \
    && chown -R caprigestor:caprigestor /app

WORKDIR /app

COPY --from=build --chown=caprigestor:caprigestor /workspace/target/CapriGestor-0.0.1-SNAPSHOT.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=prod \
    CAPRIGESTOR_MESSAGING_ENABLED=false \
    JAVA_OPTS=""

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=5 \
  CMD curl --fail --silent --show-error http://127.0.0.1:8080/actuator/health || exit 1

USER caprigestor

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

