FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/shareit-server-0.0.1-SNAPSHOT.jar"]