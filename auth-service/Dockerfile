FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/auth-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 4005
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
