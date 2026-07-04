# Java Data Stack Helm Chart

Portable umbrella chart for Cassandra, KeyDB, Kafka, and an optional Java application Deployment.

The upstream charts are vendored in `charts/`, so this directory can be copied to another machine or repository and deployed without running `helm dependency update`.

The values file points Bitnami workloads at `docker.io/bitnamilegacy/*` image repositories because the pinned image tags from these chart versions are no longer available under `docker.io/bitnami/*`.

## Included Charts

| Component | Chart | Version |
| --- | --- | --- |
| Cassandra | `bitnami/cassandra` | `12.3.11` |
| KeyDB | `bitnami/keydb` | `0.5.22` |
| Kafka | `bitnami/kafka` | `32.4.3` |

Full upstream defaults are saved under `upstream-values/`:

- `upstream-values/cassandra.values.yaml`
- `upstream-values/keydb.values.yaml`
- `upstream-values/kafka.values.yaml`

## Deploy UAT

```bash
helm upgrade --install utp ./charts/java-data-stack \
  --namespace utp \
  --create-namespace \
  -f ./charts/java-data-stack/values.yaml \
  -f ./charts/java-data-stack/values-uat.yaml
```

Check pods:

```bash
kubectl get pods -n utp
kubectl get svc -n utp
```

Internal endpoints for release `utp`:

- Cassandra: `utp-cassandra:9042`
- KeyDB: `utp-keydb-master:6379`
- Kafka: `utp-kafka:9092`

## Optional Java App

No Java source code is included. The chart only provides an optional Deployment that can run your existing image and inject connection settings.

The `-d` entry in `javaApp.args` is not a Helm option. It is only a default command-line argument passed to your Java container. Keep it only if your Java application supports a `-d` flag, for example to select debug/deploy/demo mode. Remove or replace it if your app does not use that flag.

Enable it at install time:

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

The default Java container args include:

```yaml
javaApp:
  args:
    - "-d"
```

Your app receives:

- `CASSANDRA_CONTACT_POINTS`
- `CASSANDRA_PORT`
- `CASSANDRA_LOCAL_DATACENTER`
- `CASSANDRA_KEYSPACE`
- `CASSANDRA_USERNAME`
- `CASSANDRA_PASSWORD`
- `KEYDB_HOST`
- `KEYDB_PORT`
- `KEYDB_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`

## Profiles

Use profiles as overlays on top of `values.yaml`.

```bash
# Small persistent UAT
-f values.yaml -f values-uat.yaml

# Fast local/dev, no persistence
-f values.yaml -f values-dev-ephemeral.yaml

# Single-cluster production HA
-f values.yaml -f values-prod-ha.yaml

# Cassandra multi-datacenter reference, deploy separately per DC
-f values.yaml -f values-cassandra-active-active-dc1.yaml
-f values.yaml -f values-cassandra-active-active-dc2.yaml

# KeyDB multi-master / active replica reference
-f values.yaml -f values-keydb-active-active.yaml
```

## Production Notes

Replace all placeholder passwords before deploying outside UAT. For production, prefer existing Kubernetes Secrets over inline values, and set a real `global.defaultStorageClass`.

For production images, either keep the tested `bitnamilegacy/*` repositories from `values.yaml` or switch to your approved private registry mirror. If your organization mirrors images internally, override:

```yaml
cassandra.image.repository: your-registry/cassandra
keydb.image.repository: your-registry/keydb
kafka.image.repository: your-registry/kafka
```

Cassandra active-active requires network connectivity between datacenters and stable seed addresses. The two active-active files are reference overlays; update `cluster.extraSeeds` with reachable seed DNS names or IPs for the opposite datacenter.

Kafka active-active is not enabled here because Kafka normally needs a replication layer such as MirrorMaker 2 or a platform-specific multi-cluster design. This chart provides a single-cluster HA Kafka profile.

## Export

Copy or archive the `charts/java-data-stack` directory:

```bash
tar -czf java-data-stack-chart.tgz charts/java-data-stack
```

On another machine:

```bash
tar -xzf java-data-stack-chart.tgz
helm upgrade --install utp ./charts/java-data-stack -n utp --create-namespace \
  -f ./charts/java-data-stack/values.yaml \
  -f ./charts/java-data-stack/values-uat.yaml
```
