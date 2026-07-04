# Grafana Observability Plugin Bundle

This directory contains an air-gap-ready plugin bundle for Grafana `12.4.5`.

Included plugins, matching the four cards you should see in Grafana under `Administration > Plugins and data > Plugins`:

| Plugin | Version | Purpose |
| --- | --- | --- |
| `grafana-lokiexplore-app` | `2.1.5` | Grafana Logs Drilldown |
| `grafana-metricsdrilldown-app` | `2.2.0` | Grafana Metrics Drilldown |
| `grafana-pyroscope-app` | `2.1.0` | Grafana Profiles Drilldown |
| `grafana-exploretraces-app` | `2.1.0` | Grafana Traces Drilldown |

`grafana-pyroscope-datasource` is not included because Grafana reports it as a core plugin and refuses separate installation.

## Build Image

```bash
docker build -t registry.airgap.local/monitoring/grafana:12.4.5-observability-plugins \
  observability/grafana-plugins-bundle
```

Push it to your private registry:

```bash
docker push registry.airgap.local/monitoring/grafana:12.4.5-observability-plugins
```

## Verify Archive

```bash
tar -tzf observability/grafana-plugins-bundle/grafana-observability-plugins-12.4.5.tar.gz
```

## Refresh Bundle

Run this from the repository root on an internet-connected machine:

```bash
rm -rf observability/grafana-plugins-bundle/plugins
mkdir -p observability/grafana-plugins-bundle/plugins

./bin/grafana cli --homepath tools/grafana --pluginsDir observability/grafana-plugins-bundle/plugins plugins install grafana-metricsdrilldown-app
./bin/grafana cli --homepath tools/grafana --pluginsDir observability/grafana-plugins-bundle/plugins plugins install grafana-lokiexplore-app
./bin/grafana cli --homepath tools/grafana --pluginsDir observability/grafana-plugins-bundle/plugins plugins install grafana-exploretraces-app
./bin/grafana cli --homepath tools/grafana --pluginsDir observability/grafana-plugins-bundle/plugins plugins install grafana-pyroscope-app

tar -czf observability/grafana-plugins-bundle/grafana-observability-plugins-12.4.5.tar.gz \
  -C observability/grafana-plugins-bundle/plugins \
  grafana-exploretraces-app \
  grafana-lokiexplore-app \
  grafana-metricsdrilldown-app \
  grafana-pyroscope-app
```
