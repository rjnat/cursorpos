package com.cursorpos.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for event-driven architecture.
 * 
 * <p>Configures Kafka producers and consumers with JSON serialization
 * for event publishing and consumption across microservices.</p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Slf4j
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId;

    /**
     * Kafka admin client configuration for topic management.
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Producer factory for sending events with JSON serialization.
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka template for sending events.
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Consumer factory for receiving events with JSON deserialization.
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.cursorpos.*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.Map");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Kafka listener container factory for event consumers.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(
                org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL_IMMEDIATE
        );
        return factory;
    }

    // Topic Definitions

    @Bean
    public NewTopic tenantCreatedTopic() {
        return TopicBuilder.name("tenant.created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userCreatedTopic() {
        return TopicBuilder.name("user.created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productCreatedTopic() {
        return TopicBuilder.name("product.created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productUpdatedTopic() {
        return TopicBuilder.name("product.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic inventoryUpdatedTopic() {
        return TopicBuilder.name("inventory.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionCreatedTopic() {
        return TopicBuilder.name("transaction.created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentProcessedTopic() {
        return TopicBuilder.name("payment.processed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic loyaltyPointsUpdatedTopic() {
        return TopicBuilder.name("loyalty.points.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
