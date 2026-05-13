FROM eclipse-temurin:11-jre
WORKDIR /app
COPY target/vdms-service.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "/app/app.jar"]