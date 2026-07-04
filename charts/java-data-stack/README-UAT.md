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

## Optional Java App

```bash
helm upgrade --install utp ./charts/java-data-stack \
  --namespace utp \
  --create-namespace \
  -f ./charts/java-data-stack/values.yaml \
  -f ./charts/java-data-stack/values-uat.yaml \
  --set javaApp.enabled=true \
  --set javaApp.image.repository=ghcr.io/your-org/your-java-app \
  --set javaApp.image.tag=1.0.0
```

`javaApp.args` defaults to `["-d"]`. This is passed to the Java container, not to Helm. Remove it if your application does not support a `-d` command-line flag.
