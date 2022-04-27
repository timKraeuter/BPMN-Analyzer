# Docker
Build image:
```console
docker build -t bpmnanalyzer .
```
Run image:
```console
docker run -p 4300:8080 -e PORT=4300 bpmnanalyzer
```
