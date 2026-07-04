FROM grafana/grafana:12.4.5

USER root

COPY grafana-observability-plugins-12.4.5.tar.gz /tmp/grafana-observability-plugins.tar.gz

RUN mkdir -p /usr/share/grafana/custom-plugins \
    && tar -xzf /tmp/grafana-observability-plugins.tar.gz -C /usr/share/grafana/custom-plugins \
    && chown -R 472:0 /usr/share/grafana/custom-plugins \
    && chmod -R g=u /usr/share/grafana/custom-plugins \
    && rm -f /tmp/grafana-observability-plugins.tar.gz

ENV GF_PATHS_PLUGINS=/usr/share/grafana/custom-plugins \
    GF_ANALYTICS_REPORTING_ENABLED=false \
    GF_ANALYTICS_CHECK_FOR_UPDATES=false \
    GF_ANALYTICS_CHECK_FOR_PLUGIN_UPDATES=false \
    GF_ANALYTICS_FEEDBACK_LINKS_ENABLED=false \
    GF_SNAPSHOTS_ENABLED=false \
    GF_SNAPSHOTS_EXTERNAL_ENABLED=false \
    GF_NEWS_NEWS_FEED_ENABLED=false \
    GF_PLUGINS_PLUGIN_ADMIN_ENABLED=false \
    GF_PLUGINS_PLUGIN_ADMIN_EXTERNAL_MANAGE_ENABLED=false \
    GF_PLUGINS_PLUGIN_CATALOG_URL="" \
    GF_PLUGINS_PUBLIC_KEY_RETRIEVAL_DISABLED=true \
    GF_PLUGINS_PUBLIC_KEY_RETRIEVAL_ON_STARTUP=false \
    GF_PLUGINS_PREINSTALL="" \
    GF_PLUGINS_PREINSTALL_SYNC="" \
    GF_PLUGINS_PREINSTALL_DISABLED=true \
    GF_PLUGINS_PREINSTALL_AUTO_UPDATE=false \
    GF_GRAFANA_COM_URL="" \
    GF_GRAFANA_COM_API_URL="" \
    GF_INSTALL_PLUGINS=""

USER 472
