package com.example.datastack;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class DemoService {
    private final DemoProperties properties;
    private final AtomicReference<DemoResult> lastResult = new AtomicReference<>(DemoResult.empty());

    public DemoService(DemoProperties properties) {
        this.properties = properties;
    }

    public DemoResult lastResult() {
        return lastResult.get();
    }

    public DemoResult runDemo() {
        UUID eventId = UUID.randomUUID();
        try {
            Map<String, String> cassandra = verifyCassandra(eventId);
            Map<String, String> keydb = verifyKeydb(eventId);
            Map<String, String> kafka = verifyKafka(eventId);
            DemoResult result = new DemoResult(true, Instant.now(), eventId, cassandra, keydb, kafka, null);
            lastResult.set(result);
            return result;
        } catch (Exception ex) {
            DemoResult result = new DemoResult(false, Instant.now(), eventId, Map.of(), Map.of(), Map.of(), ex.toString());
            lastResult.set(result);
            return result;
        }
    }

    private Map<String, String> verifyCassandra(UUID eventId) {
        try (CqlSession session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(properties.cassandraHost, properties.cassandraPort))
                .withLocalDatacenter(properties.cassandraLocalDatacenter)
                .withAuthCredentials(properties.cassandraUsername, properties.cassandraPassword)
                .build()) {
            session.execute("CREATE KEYSPACE IF NOT EXISTS " + properties.cassandraKeyspace
                    + " WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}");
            session.execute("CREATE TABLE IF NOT EXISTS " + properties.cassandraKeyspace
                    + ".events (id uuid PRIMARY KEY, event_type text, payload text, created_at timestamp)");
            session.execute("INSERT INTO " + properties.cassandraKeyspace
                    + ".events (id, event_type, payload, created_at) VALUES (?, ?, ?, ?)",
                    eventId, "demo", "cassandra-keydb-kafka", Instant.now());
            Row row = session.execute("SELECT event_type, payload FROM " + properties.cassandraKeyspace
                    + ".events WHERE id = ?", eventId).one();
            return Map.of(
                    "host", properties.cassandraHost,
                    "keyspace", properties.cassandraKeyspace,
                    "eventType", row == null ? "" : row.getString("event_type"),
                    "payload", row == null ? "" : row.getString("payload"));
        }
    }

    private Map<String, String> verifyKeydb(UUID eventId) {
        String key = "demo:" + eventId;
        try (Jedis jedis = new Jedis(properties.keydbHost, properties.keydbPort)) {
            if (properties.keydbPassword != null && !properties.keydbPassword.isBlank()) {
                jedis.auth(properties.keydbPassword);
            }
            jedis.setex(key, 300, "keydb-ok");
            return Map.of("host", properties.keydbHost, "key", key, "value", jedis.get(key));
        }
    }

    private Map<String, String> verifyKafka(UUID eventId) throws Exception {
        createTopic();
        String payload = "kafka-ok:" + eventId;
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.kafkaBootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        RecordMetadata metadata;
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
            metadata = producer.send(new ProducerRecord<>(properties.kafkaTopic, eventId.toString(), payload)).get();
        }

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.kafkaBootstrapServers);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            TopicPartition partition = new TopicPartition(metadata.topic(), metadata.partition());
            consumer.assign(List.of(partition));
            consumer.seek(partition, metadata.offset());
            Instant deadline = Instant.now().plusSeconds(10);
            while (Instant.now().isBefore(deadline)) {
                for (var record : consumer.poll(Duration.ofMillis(500))) {
                    if (record.offset() == metadata.offset() && eventId.toString().equals(record.key())) {
                        return Map.of(
                                "bootstrapServers", properties.kafkaBootstrapServers,
                                "topic", properties.kafkaTopic,
                                "partition", Integer.toString(record.partition()),
                                "offset", Long.toString(record.offset()),
                                "value", record.value());
                    }
                }
            }
        }
        throw new IllegalStateException("Produced Kafka message was not consumed before timeout");
    }

    private void createTopic() throws Exception {
        Properties adminProps = new Properties();
        adminProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.kafkaBootstrapServers);
        try (AdminClient admin = AdminClient.create(adminProps)) {
            try {
                admin.createTopics(List.of(new NewTopic(properties.kafkaTopic, 1, (short) 1))).all().get();
            } catch (Exception ex) {
                if (!(ex.getCause() instanceof TopicExistsException)) {
                    throw ex;
                }
            }
        }
    }
}
