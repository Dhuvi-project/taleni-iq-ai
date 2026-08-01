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
# -XX:MaxRAMPercentage caps the heap relative to the container's actual memory limit (Render's
# free tier gives 512MB) instead of the JVM's default heap sizing, which can otherwise size the
# heap against host memory it can see but doesn't actually have, leading to OOM-kills and a
# restart crash-loop.
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=70.0", "-XX:MaxMetaspaceSize=128m", "-jar", "/app/app.jar"]
