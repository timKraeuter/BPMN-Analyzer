FROM openjdk:17.0.2
ARG JAR_FILE=server/build/libs/*.jar
COPY ${JAR_FILE} app.jar
ADD /groove/ /groove/
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080/tcp