# syntax=docker/dockerfile:1.7

######################################
# Build stage
######################################
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Pre-copy pom and wrapper to leverage Docker layer caching
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn

# Resolve dependencies
RUN ./mvnw -q -B dependency:go-offline

# Copy source and build
COPY src src
RUN ./mvnw -q -B clean package -Dspotless.check.skip=true -DskipTests

######################################
# Runtime stage
######################################
FROM eclipse-temurin:21-jre

WORKDIR /app

ENV JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE=prod

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
