# ---- build stage ----------------------------------------------------
# The whole reactor is copied so the module can resolve common-lib from
# source; -pl/-am builds only this service and the modules it depends on.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY service-registry/pom.xml service-registry/
COPY api-gateway/pom.xml api-gateway/
COPY identity-service/pom.xml identity-service/
COPY project-service/pom.xml project-service/
COPY ai-service/pom.xml ai-service/
# Warm the dependency cache in its own layer so source edits do not re-download.
RUN mvn -B -q dependency:go-offline -pl identity-service -am || true
COPY . .
RUN mvn -B clean package -pl identity-service -am -DskipTests

# ---- runtime stage --------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Runs unprivileged: a container compromise should not yield root.
RUN addgroup -S teamflow && adduser -S teamflow -G teamflow
COPY --from=build /build/identity-service/target/identity-service.jar app.jar
RUN chown -R teamflow:teamflow /app
USER teamflow
EXPOSE 8081
# MaxRAMPercentage lets the JVM respect the container memory limit instead of
# sizing the heap from the host's total RAM.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
