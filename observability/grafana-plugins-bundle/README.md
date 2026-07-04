# Grafana Observability Plugin Bundle

This directory contains an air-gap-ready plugin bundle for the custom image tag `12.4.6-observability-plugins`.

The Dockerfile still uses `grafana/grafana:12.4.5` as the base image because an upstream `grafana/grafana:12.4.6` image was not available when this bundle was created. The `12.4.6-observability-plugins` tag is your custom release tag.

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
docker build -t registry.airgap.local/monitoring/grafana:12.4.6-observability-plugins \
  observability/grafana-plugins-bundle
```

Push it to your private registry:

```bash
docker push registry.airgap.local/monitoring/grafana:12.4.6-observability-plugins
```

## Air-Gapped Runtime Settings

The Dockerfile installs plugins into `/usr/share/grafana/custom-plugins` and sets `GF_PATHS_PLUGINS` to that directory. This avoids kube-prometheus-stack PVC mounts hiding plugins under `/var/lib/grafana/plugins`.

The Dockerfile disables Grafana.com calls that are not useful in an air-gapped cluster:

- update checks
- plugin update checks
- plugin catalog URL
- plugin admin install/update UI
- plugin preinstall and preinstall auto-update
- snapshot publishing
- news feed
- feedback links
- plugin public-key retrieval

Do not set `GF_INSTALL_PLUGINS` in Helm, Kubernetes, or the container environment. The plugins are already baked into `/usr/share/grafana/custom-plugins`.

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
