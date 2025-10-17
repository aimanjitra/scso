# Stage 1: build with Maven
FROM maven:3.10.1-eclipse-temurin-21 AS build
WORKDIR /src
# copy pom + sources
COPY my-app/pom.xml my-app/
COPY my-app/src my-app/src
# build shaded jar
WORKDIR /src/my-app
RUN mvn -DskipTests clean package

# Stage 2: runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
# copy the shaded jar from the build stage
COPY --from=build /src/my-app/target/*-shaded.jar /app/app.jar
# Expose port (Render sets PORT env; Spark uses it)
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
