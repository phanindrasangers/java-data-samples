# Sample Java Data Stack App

This is a small Spring Boot application that verifies Cassandra, KeyDB, and Kafka connectivity.

`-d` means demo mode. When the container starts with `-d`, it runs one end-to-end demo operation:

1. Creates or verifies the Cassandra keyspace/table.
2. Inserts and reads one Cassandra event.
3. Writes and reads one KeyDB value.
4. Creates a Kafka topic if needed, produces one message, and consumes it back.

## Build

```bash
docker build -t sample-java-app:0.1.0 ./sample-java-app
rm -f ./charts/java-data-stack/images/sample-java-app-0.1.0.tar.gz.part-*
docker save sample-java-app:0.1.0 | gzip -9 | split -b 45m -d -a 2 - ./charts/java-data-stack/images/sample-java-app-0.1.0.tar.gz.part-
```

For kind:

```bash
cat ./charts/java-data-stack/images/sample-java-app-0.1.0.tar.gz.part-* | gunzip | docker load
kind load docker-image sample-java-app:0.1.0 --name kind
```

## Run In Helm

```bash
helm upgrade --install utp ./charts/java-data-stack \
  -n utp \
  --create-namespace \
  -f ./charts/java-data-stack/values.yaml \
  -f ./charts/java-data-stack/values-uat.yaml \
  --set javaApp.enabled=true
```

## Endpoints

- `GET /health`
- `GET /last`
- `POST /demo`

Port-forward:

```bash
kubectl port-forward -n utp svc/utp-java-data-stack-java-app 8080:8080
curl http://localhost:8080/health
curl -X POST http://localhost:8080/demo
```
