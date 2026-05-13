FROM eclipse-temurin:11-jre
WORKDIR /app
COPY target/sclera.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/app.jar"]
