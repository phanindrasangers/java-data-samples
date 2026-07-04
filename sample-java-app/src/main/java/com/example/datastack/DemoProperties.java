package com.example.datastack;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DemoProperties {
    @Value("${CASSANDRA_CONTACT_POINTS:localhost}")
    String cassandraHost;

    @Value("${CASSANDRA_PORT:9042}")
    int cassandraPort;

    @Value("${CASSANDRA_LOCAL_DATACENTER:datacenter1}")
    String cassandraLocalDatacenter;

    @Value("${CASSANDRA_KEYSPACE:app_uat}")
    String cassandraKeyspace;

    @Value("${CASSANDRA_USERNAME:cassandra}")
    String cassandraUsername;

    @Value("${CASSANDRA_PASSWORD:}")
    String cassandraPassword;

    @Value("${KEYDB_HOST:localhost}")
    String keydbHost;

    @Value("${KEYDB_PORT:6379}")
    int keydbPort;

    @Value("${KEYDB_PASSWORD:}")
    String keydbPassword;

    @Value("${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}")
    String kafkaBootstrapServers;

    @Value("${KAFKA_TOPIC:demo-events}")
    String kafkaTopic;
}
