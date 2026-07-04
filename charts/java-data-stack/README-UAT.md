# UAT Deployment

This profile deploys a small persistent stack suitable for UAT on a single Kubernetes cluster.

## Install

```bash
helm upgrade --install utp ./charts/java-data-stack \
  --namespace utp \
  --create-namespace \
  -f ./charts/java-data-stack/values.yaml \
  -f ./charts/java-data-stack/values-uat.yaml \
  --timeout 10m \
  --wait
```

## Verify

```bash
helm status utp -n utp
kubectl get pods,pvc,svc -n utp
kubectl rollout status statefulset/utp-cassandra -n utp --timeout=10m
kubectl rollout status statefulset/utp-kafka-controller -n utp --timeout=10m
kubectl rollout status statefulset/utp-keydb-master -n utp --timeout=10m
kubectl rollout status statefulset/utp-keydb-replica -n utp --timeout=10m
```

Expected internal endpoints for release `utp`:

- Cassandra: `utp-cassandra:9042`
- KeyDB master: `utp-keydb-master:6379`
- KeyDB replica: `utp-keydb-replica:6379`
- Kafka: `utp-kafka:9092`

## Optional Java Demo App

Load the included sample app image before enabling it on kind:

```bash
cat ./charts/java-data-stack/images/sample-java-app-0.1.0.tar.gz.part-* | gunzip | docker load
kind load docker-image sample-java-app:0.1.0 --name kind
```

```bash
helm upgrade --install utp ./charts/java-data-stack \
  --namespace utp \
  --create-namespace \
  -f ./charts/java-data-stack/values.yaml \
  -f ./charts/java-data-stack/values-uat.yaml \
  --set javaApp.enabled=true \
  --timeout 10m \
  --wait
```

`javaApp.args` defaults to `["-d"]`. This is passed to the Java container, not to Helm. For the included app, `-d` runs the demo flow against Cassandra, KeyDB, and Kafka during startup.

Check the app:

```bash
kubectl logs deploy/utp-java-data-stack-java-app -n utp
kubectl port-forward svc/utp-java-data-stack-java-app -n utp 18080:8080
curl http://127.0.0.1:18080/last
```
