# Air-Gapped Grafana Drilldowns With Loki And Tempo

These files add Loki, Tempo, and Alloy beside an existing kube-prometheus-stack install.

Use this when Grafana is already running from kube-prometheus-stack and you want the Grafana 12 Drilldown entries to have real data sources:

- Metrics: existing Prometheus from kube-prometheus-stack.
- Logs: Loki plus Alloy pod log collection.
- Traces: Tempo plus Alloy OTLP receiver.

## Files

- `kube-prometheus-stack-grafana-values.yaml`: custom Grafana `12.4.6-observability-plugins` image and Prometheus/Loki/Tempo data sources.
- `grafana-plugins-bundle/`: air-gap plugin archive and Dockerfile for Grafana Metrics, Logs, Traces, and Profiles plugins.
- `loki-values-airgap.yaml`: small monolithic Loki install for UAT or small clusters.
- `tempo-values-airgap.yaml`: small monolithic Tempo install with OTLP receivers.
- `alloy-values-airgap.yaml`: DaemonSet that collects pod logs and forwards them to Loki, and exposes OTLP for traces.

Replace every `registry.airgap.local` value with your private registry.

## Grafana Plugin Image

Build and push the bundled Grafana image before upgrading kube-prometheus-stack:

```bash
docker build -t registry.airgap.local/monitoring/grafana:12.4.6-observability-plugins \
  observability/grafana-plugins-bundle

docker push registry.airgap.local/monitoring/grafana:12.4.6-observability-plugins
```

The Dockerfile bakes exactly these Drilldown plugins into `/usr/share/grafana/custom-plugins`:

- `grafana-lokiexplore-app`: Grafana Logs Drilldown
- `grafana-metricsdrilldown-app`: Grafana Metrics Drilldown
- `grafana-pyroscope-app`: Grafana Profiles Drilldown
- `grafana-exploretraces-app`: Grafana Traces Drilldown

The image and `kube-prometheus-stack-grafana-values.yaml` also disable Grafana.com update checks, plugin catalog access, plugin preinstall, snapshot publishing, news feed, and feedback links. Keep `grafana.plugins: []` and do not set `GF_INSTALL_PLUGINS` in an air-gapped cluster.

`/usr/share/grafana/custom-plugins` is used instead of `/var/lib/grafana/plugins` because kube-prometheus-stack often mounts Grafana's PVC at `/var/lib/grafana`, which hides plugins baked into that path.

## Install Order

Install kube-prometheus-stack first or upgrade your existing release with the Grafana datasource overlay:

```bash
helm upgrade --install kube-prometheus-stack prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --version 41.7.3 \
  -f observability/kube-prometheus-stack-grafana-values.yaml
```

Install Loki and Tempo in the same namespace:

```bash
helm upgrade --install loki grafana/loki \
  --namespace monitoring \
  -f observability/loki-values-airgap.yaml

helm upgrade --install tempo grafana/tempo \
  --namespace monitoring \
  -f observability/tempo-values-airgap.yaml
```

Install Alloy after Loki and Tempo:

```bash
helm upgrade --install alloy grafana/alloy \
  --namespace monitoring \
  -f observability/alloy-values-airgap.yaml
```

## Air-Gap Preparation

On an internet-connected machine, pull the charts and images, then move them into your private registry and internal chart repository.

```bash
helm pull prometheus-community/kube-prometheus-stack --version 41.7.3
helm pull grafana/loki
helm pull grafana/tempo
helm pull grafana/alloy
```

Mirror at least these images:

```text
grafana/grafana:12.4.5
grafana/loki
grafana/tempo
grafana/alloy
```

The exact Loki, Tempo, and Alloy tags depend on the chart versions you vendor. Confirm them with:

```bash
helm template loki grafana/loki -f observability/loki-values-airgap.yaml | grep 'image:'
helm template tempo grafana/tempo -f observability/tempo-values-airgap.yaml | grep 'image:'
helm template alloy grafana/alloy -f observability/alloy-values-airgap.yaml | grep 'image:'
```

## Verify

```bash
kubectl get pods -n monitoring
kubectl logs -n monitoring deploy/kube-prometheus-stack-grafana
```

In Grafana, check:

- Connections > Data sources: `Prometheus`, `Loki`, `Tempo`
- Drilldown > Metrics
- Drilldown > Logs
- Drilldown > Traces

Quick API checks from inside the cluster:

```bash
kubectl run -n monitoring tmp-curl --rm -it --image=registry.airgap.local/curlimages/curl:latest --restart=Never -- \
  curl -sf http://loki-gateway.monitoring.svc.cluster.local/loki/api/v1/labels

kubectl run -n monitoring tmp-curl --rm -it --image=registry.airgap.local/curlimages/curl:latest --restart=Never -- \
  curl -sf http://tempo.monitoring.svc.cluster.local:3100/ready
```

## Notes

This is a small-cluster/UAT starting point. For production, move Loki and Tempo storage from filesystem/PVC to object storage, raise replicas, and size retention according to your volume.
