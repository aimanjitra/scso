# Stage 1: build with Maven + JDK 21
FROM maven:3.10.1-openjdk-21 AS build
WORKDIR /src

# Copy POM and source tree
COPY my-app/pom.xml my-app/
COPY my-app/src my-app/src

WORKDIR /src/my-app

# Build shaded jar (skip tests)
RUN mvn -DskipTests clean package

# Stage 2: small runtime image with JRE 21
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the shaded jar from the build stage
COPY --from=build /src/my-app/target/*-shaded.jar /app/app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
