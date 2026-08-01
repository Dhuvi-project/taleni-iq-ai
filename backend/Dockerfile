# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests clean package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S talentiq && adduser -S talentiq -G talentiq
COPY --from=build /build/target/talentiq-backend.jar /app/app.jar
RUN mkdir -p /app/uploads && chown -R talentiq:talentiq /app
USER talentiq
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
