# Stage 1: build
# Start with a Maven image that includes JDK 21
FROM maven:3.9.11-amazoncorretto-21 AS build

# Copy source code and pom.xml file to /app folder
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build source code with maven
RUN mvn -q -B clean package -Dspotless.check.skip=true -DskipTests

#Stage 2: create image
# Start with Amazon Correto JDK 21
FROM amazoncorretto:21.0.9

ARG PROFILE=dev
ARG APP_VERSION=1.1.0

# Set working folder to App and copy complied file from above step
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV ACTIVE_PROFILE=${PROFILE}
ENV JAR_VERSION=${APP_VERSION}

CMD java -jar -Dspring.profiles.active=${ACTIVE_PROFILE} per-${JAR_VERSION}.jar

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]