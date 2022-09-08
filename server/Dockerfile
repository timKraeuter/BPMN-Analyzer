FROM openjdk:11.0.15
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ADD /groove/ /groove/
ENTRYPOINT ["java","-jar","/app.jar"]