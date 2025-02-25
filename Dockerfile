FROM openjdk:17
WORKDIR /app
COPY target/retail-app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]