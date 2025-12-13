FROM gcr.io/distroless/java25-debian13
ARG JAR_FILE=server/build/libs/ruleGeneratorServer-2.0.1.jar
COPY ${JAR_FILE} app.jar
ADD /groove/ /groove/
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080/tcp