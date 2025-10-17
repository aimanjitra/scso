# Stage 1: build with JDK21 and install Maven (guarantees Java 21 build)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src

# Install Maven (Debian-based images)
RUN apt-get update && \
    apt-get install -y --no-install-recommends maven && \
    rm -rf /var/lib/apt/lists/*

# Copy sources
COPY my-app/pom.xml my-app/
COPY my-app/src my-app/src

WORKDIR /src/my-app

# Build shaded jar (skip tests)
RUN mvn -DskipTests clean package

# Stage 2: small runtime image with JRE21
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the shaded jar from the build stage
COPY --from=build /src/my-app/target/*-shaded.jar /app/app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
