# Production Deployment

Use `values-prod-ha.yaml` as the starting point for a single-cluster HA deployment.

## Before Install

Review and replace:

- `global.defaultStorageClass`
- Cassandra, KeyDB, and Kafka passwords
- resource requests and limits
- image repository policy, especially if you use an internal registry mirror
- Kafka authentication and listener settings

For production, prefer existing Kubernetes Secrets instead of inline passwords in values files.

## Install

```bash
helm upgrade --install utp-prod ./charts/java-data-stack \
  --namespace utp-prod \
  --create-namespace \
  -f ./charts/java-data-stack/values.yaml \
  -f ./charts/java-data-stack/values-prod-ha.yaml \
  --timeout 20m \
  --wait
```

## Verify

```bash
helm status utp-prod -n utp-prod
kubectl get pods,pvc,svc -n utp-prod
kubectl rollout status statefulset/utp-prod-cassandra -n utp-prod --timeout=20m
kubectl rollout status statefulset/utp-prod-kafka-controller -n utp-prod --timeout=20m
kubectl rollout status statefulset/utp-prod-keydb-master -n utp-prod --timeout=20m
kubectl rollout status statefulset/utp-prod-keydb-replica -n utp-prod --timeout=20m
```

## Active-Active Notes

Cassandra active-active is provided as reference overlays:

- `values-cassandra-active-active-dc1.yaml`
- `values-cassandra-active-active-dc2.yaml`

Deploy each overlay in its own datacenter and update `cluster.extraSeeds` with reachable seed addresses from the opposite datacenter.

KeyDB multi-master / active replica is provided as:

- `values-keydb-active-active.yaml`

Use it only after testing application-level conflict behavior.

Kafka active-active is not configured in this chart. For multi-cluster Kafka, plan MirrorMaker 2 or another replication strategy separately.
