FROM eclipse-temurin:21-jdk as builder

WORKDIR /app

COPY /target/recipe-1.0.0.jar app.jar
#COPY src/main/resources/schema.sql /app/schema.sql

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD java -cp app.jar org.springframework.boot.loader.JarLauncher -c "curl -f http://localhost:8080/actuator/health || exit 1"

# Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]

# Labels
LABEL maintainer="ABN AMRO Recipe API"
LABEL version="1.0.0"
LABEL description="Recipe Management API - Production Ready"

