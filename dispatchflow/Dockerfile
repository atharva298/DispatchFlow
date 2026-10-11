# Build with Maven and Java 21; only the packaged application enters the runtime image.
FROM maven:3.9.16-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

# Keep dependency downloads in a separate cached layer from application source.
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21.0.12_8-jre-alpine-3.24 AS runtime

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build --chown=spring:spring /workspace/target/dispatchflow-*.jar /app/app.jar

USER spring:spring
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=45s --retries=3 \
    CMD wget -q -O /dev/null http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
