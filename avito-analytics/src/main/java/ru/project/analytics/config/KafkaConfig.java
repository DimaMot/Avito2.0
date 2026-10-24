package ru.project.analytics.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.project.commonlib.kafka.ItemSearchEvent;
import ru.project.commonlib.kafka.ItemViewEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaConfig {
    private final Environment env;
    private final KafkaProperties kafkaProperties;

    @Bean
    public ConsumerFactory<String, ItemSearchEvent> itemSearchEventConsumerFactory() {
        return buildConsumerFactory(ItemSearchEvent.class, env.getProperty("spring.kafka.consumer.search-group-id"));
    }

    @Bean
    public ConsumerFactory<String, ItemViewEvent> itemViewEventConsumerFactory() {
        return buildConsumerFactory(ItemViewEvent.class, env.getProperty("spring.kafka.consumer.view-group-id"));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ItemSearchEvent> itemSearchEventListener(
            ConsumerFactory<String, ItemSearchEvent> itemSearchEventConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, ItemSearchEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(itemSearchEventConsumerFactory);
        factory.setBatchListener(true);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ItemViewEvent> itemViewEventListener(
            ConsumerFactory<String, ItemViewEvent> itemViewEventConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, ItemViewEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(itemViewEventConsumerFactory);
        factory.setBatchListener(true);
        return factory;
    }

    private <T> ConsumerFactory<String, T> buildConsumerFactory(Class<T> eventType, String groupId) {
        Map<String, Object> prop = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
        prop.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        JsonDeserializer<T> jsonDeserializer = new JsonDeserializer<>(eventType);
        jsonDeserializer.addTrustedPackages("ru.project.commonlib.kafka");
        jsonDeserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                prop,
                new StringDeserializer(),
                jsonDeserializer
        );
    }
}
