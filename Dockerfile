# ─── Stage 1: Build ───────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy gradle files first (better Docker layer caching)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies
RUN ./gradlew dependencies --no-daemon || true

# Copy source and build JAR
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# ─── Stage 2: Run ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Copy only the built JAR from build stage
COPY --from=build /app/build/libs/fillinus-erp.jar app.jar

# Expose port (Render injects $PORT)
EXPOSE 8080

ENTRYPOINT ["java", "-Xmx450m", "-jar", "app.jar"]
