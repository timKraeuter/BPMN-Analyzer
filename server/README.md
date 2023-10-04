# Running the application locally

## IDE

Start the main method in the class **Application**.
This will start the application at [localhost:8080](http://localhost:8080/).

## Jar File

Run the application (execute in the **Groove_Rule_Generation**, i.e., root directory):

```bash
java -jar build/libs/ruleGeneratorServer-2.0.0.jar
```

This will start the application at [localhost:8080](http://localhost:8080/).

Run the application on a specific port, for example 4300 (execute in the **Groove_Rule_Generation**,
i.e., root directory):

```bash
java -jar build/libs/ruleGeneratorServer-2.0.0.jar --server.port=4300
```

This will start the application with the specified port.

## Docker

Build the application image:

```bash
cd ..
docker build -t bpmnanalyzer .
```

Run the application image:

```bash
docker run -p 8080:8080 bpmnanalyzer
```

This will start the application at [localhost:8080](http://localhost:8080/).

# Deployment to Azure

1. Build the container (see Docker section).

2. Tag container image

```bash
docker tag bpmnanalyzer tg2022.azurecr.io/bpmnanalyzer:v1
```

3. Login to the Container Registry. Environment variables `APP_ID` and `AZURE_PW` are expected to be
   set (export APP_ID=<app-id> etc.).

```bash
docker login tg2022.azurecr.io --username $APP_ID --password $AZURE_PW
```

4. Push image to Container Registry

```bash
docker push tg2022.azurecr.io/bpmnanalyzer:v1
```

5. Create new container app revision in the GUI.