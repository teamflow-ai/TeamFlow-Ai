# ---------- Build stage ----------
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests -B

# ---------- Runtime stage ----------
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S teamflow && adduser -S teamflow -G teamflow
COPY --from=build /workspace/target/teamflow-ai-backend.jar app.jar
RUN chown teamflow:teamflow app.jar
USER teamflow

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
