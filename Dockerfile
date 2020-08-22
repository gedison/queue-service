FROM openjdk:14-jdk
ARG JAR_FILE=blob-service-core/target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]