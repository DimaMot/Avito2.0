package ru.project.item.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.project.commonlib.kafka.ItemSearchEvent;
import ru.project.commonlib.kafka.ItemViewEvent;

import java.util.HashMap;
import java.util.Map;

@Profile("!test")
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    private final Environment env;

    @Bean
    public ProducerFactory<String, ItemViewEvent> itemViewEventProducerFactory() {
        Map<String, Object> prop = baseProducer();
        return new DefaultKafkaProducerFactory<>(prop);
    }

    @Bean
    public ProducerFactory<String, ItemSearchEvent> itemSearchEventProducerFactory() {
        Map<String, Object> prop = baseProducer();
        return new DefaultKafkaProducerFactory<>(prop);
    }

    @Bean
    public KafkaTemplate<String, ItemViewEvent> viewEventKafkaTemplate(ProducerFactory<String, ItemViewEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, ItemSearchEvent> searchEventKafkaTemplate(ProducerFactory<String, ItemSearchEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    private Map<String, Object> baseProducer() {
        Map<String, Object> prop = new HashMap<>();
        prop.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, env.getProperty("spring.kafka.bootstrap-servers"));
        prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, env.getProperty("spring.kafka.producer.key-serializer"));
        prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, env.getProperty("spring.kafka.producer.value-serializer"));
        prop.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return prop;
    }
}
