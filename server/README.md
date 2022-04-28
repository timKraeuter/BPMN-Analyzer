# Docker
Build image:
```console
docker build -t bpmnanalyzer .
```
Run image:
```console
docker run -p 8080:8080 bpmnanalyzer
```

Deploy to Heroku:
```console
heroku container:push web
```
```console
heroku container:release web
```