# Build stage
FROM maven:3.8.4-openjdk-11-slim AS build
WORKDIR /app

# Copy source code to the container
COPY src ./src
COPY pom.xml .

# Build the application
RUN mvn clean package

# Run stage
FROM adoptopenjdk/openjdk11:jre-11.0.9_11.1-alpine
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/kube-memory-1.0-SNAPSHOT.jar .

# Expose the port the server listens on
EXPOSE 8000

USER 10014
# Command to run the application
CMD ["java", "-jar", "kube-memory-1.0-SNAPSHOT.jar"]