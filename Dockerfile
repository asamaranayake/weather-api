# Stage 1: Build the application
FROM maven:3.8.5-openjdk-11 AS build

#Install git
RUN apt-get update && apt-get install -y git

# Set working directory
WORKDIR /app

#Clone the repository
RUN git clone https://github.com/asamaranayake/weather-api.git .


RUN mvn clean install -DskipTests

# Stage 2: Run the application
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/weather-api-1.0-SNAPSHOT.jar .
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "weather-api-1.0-SNAPSHOT.jar"] 