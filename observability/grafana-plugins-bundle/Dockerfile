FROM grafana/grafana:12.4.5

USER root

COPY grafana-observability-plugins-12.4.5.tar.gz /tmp/grafana-observability-plugins.tar.gz

RUN mkdir -p /var/lib/grafana/plugins \
    && tar -xzf /tmp/grafana-observability-plugins.tar.gz -C /var/lib/grafana/plugins \
    && chown -R 472:0 /var/lib/grafana/plugins \
    && chmod -R g=u /var/lib/grafana/plugins \
    && rm -f /tmp/grafana-observability-plugins.tar.gz

ENV GF_ANALYTICS_REPORTING_ENABLED=false \
    GF_ANALYTICS_CHECK_FOR_UPDATES=false \
    GF_ANALYTICS_CHECK_FOR_PLUGIN_UPDATES=false \
    GF_PLUGINS_PLUGIN_ADMIN_ENABLED=false \
    GF_PLUGINS_PLUGIN_ADMIN_EXTERNAL_MANAGE_ENABLED=false

USER 472

