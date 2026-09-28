FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn --batch-mode clean package -DskipTests

FROM amazoncorretto:21
WORKDIR /app
COPY --from=build /workspace/target/concurrent-application-server-1.0.0.jar app.jar
ENV PORT=6000
EXPOSE 6000
ENTRYPOINT ["java", "-jar", "app.jar"]
