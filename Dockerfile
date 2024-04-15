FROM amazoncorretto:21-al2023-headless
ARG JAR_FILE=server/build/libs/ruleGeneratorServer-2.0.0.jar
COPY ${JAR_FILE} app.jar
ADD /groove/ /groove/
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080/tcp