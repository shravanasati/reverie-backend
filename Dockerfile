# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Security scan (optional but recommended)
# FROM aquasec/trivy:latest AS security-scan
# COPY --from=builder /app/target/*.jar /app/application.jar
# RUN trivy filesystem --no-progress --exit-code 0 /app/application.jar

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add necessary production packages
RUN apk add --no-cache tzdata curl

# Add non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Create necessary directories with proper permissions
RUN mkdir /app/logs && \
	chown -R spring:spring /app

# Set timezone
ENV TZ=UTC

# Copy jar from builder stage
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

# Configure JVM options for production
ENV JAVA_OPTS="-XX:+UseContainerSupport \
	-XX:MaxRAMPercentage=75.0 \
	-XX:+HeapDumpOnOutOfMemoryError \
	-XX:HeapDumpPath=/app/logs/heap-dump.hprof \
	-XX:+ExitOnOutOfMemoryError \
	-Xlog:gc*:/app/logs/gc.log:time \
	-Duser.timezone=UTC"

# Production environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE $SERVER_PORT

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
	CMD curl -f http://localhost:$SERVER_PORT/actuator/health || exit 1

# Define volumes for logs and persistent data
VOLUME ["/app/logs"]

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
