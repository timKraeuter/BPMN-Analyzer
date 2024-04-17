# Running the application locally

## Docker (using docker hub)

The image is available through [docker hub](https://hub.docker.com/r/tkra/bpmn-analyzer).

Run the following script on your machine (docker installation required):

```bash
docker pull tkra/bpmn-analyzer
docker run -p 8080:8080 tkra/bpmn-analyzer
```

This will start the application at [localhost:8080](http://localhost:8080/).
The image is updated using a GitHub action, which builds and pushes the image to docker hub (see release.yml).

## IDE

Start the main method in the class **Application**.
This will start the application at [localhost:8080](http://localhost:8080/).

## Jar File

Run the application (execute in this directory):

```bash
java -jar libs/ruleGeneratorServer-2.0.0.jar
```

This will start the application at [localhost:8080](http://localhost:8080/).

Run the application on a specific port, for example, 4300:

```bash
java -jar libs/ruleGeneratorServer-2.0.0.jar --server.port=4300
```

This will start the application with the specified port.
