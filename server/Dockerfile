FROM openjdk:11.0.15
ARG JAR_FILE=build/libs/*.jar
ARG PORT=8080
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar", "--server.port=${PORT}"]