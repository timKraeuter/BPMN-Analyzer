# Running the application locally
## IDE
Start the main method in the class **Application**.
This will start the application at [localhost:8080](http://localhost:8080/).

## Jar File
Run the application (execute in the **Groove_Rule_Generation**, i.e., root directory):
```console
java -jar ruleGeneratorServer-1.0.0.jar
```
This will start the application at [localhost:8080](http://localhost:8080/).

Run the application on a specific port, for example 4300 (execute in this directory):
```console
java -jar ruleGeneratorServer-1.0.0.jar --server.port=4300
```
This will start the application with the specified port.

## Docker
Build the application image:
```console
docker build -t bpmnanalyzer .
```

Run the application image:
```console
docker run -p 8080:8080 bpmnanalyzer
```
This will start the application at [localhost:8080](http://localhost:8080/).

# Deployment to Heroku
Change the application.properties file.

Login:
```console
heroku login
```
Container login:
```console
heroku container:login
```

Push the container:
```console
heroku container:push web
```

Release the container:
```console
heroku container:release web
```