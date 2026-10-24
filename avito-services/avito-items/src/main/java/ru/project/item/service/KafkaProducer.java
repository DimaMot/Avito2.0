package ru.project.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.project.commonlib.kafka.ItemSearchEvent;
import ru.project.commonlib.kafka.ItemViewEvent;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, ItemViewEvent> viewEventKafkaTemplate;
    private final KafkaTemplate<String, ItemSearchEvent> searchEventKafkaTemplate;

    @Value("${spring.kafka.topic.view}")
    private String viewTopic;

    @Value("${spring.kafka.topic.search}")
    private String searchTopic;

    public void sendEvent(long itemId, long userId, String city, Instant timestamp) {
        ItemViewEvent viewEvent = new ItemViewEvent(
                itemId,
                userId,
                city,
                timestamp
        );
        log.info("Отправка ивента: на topic={}, itemId={}", viewTopic, itemId);
        viewEventKafkaTemplate.send(
                viewTopic,
                String.valueOf(itemId),
                viewEvent
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Ошибка отправки: itemId={}, error={}", itemId, ex.getMessage(), ex);
            }
        });
    }

    public void sendEvent(String query, Long userId, String city, int resultCount) {
        ItemSearchEvent searchEvent = new ItemSearchEvent(
                query,
                userId,
                city,
                resultCount,
                Instant.now()
        );
        log.info("Отправка ивента: на topic={}", searchTopic);
        searchEventKafkaTemplate.send(
                searchTopic,
                String.valueOf(query),
                searchEvent
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Ошибка отправки: query={}, error={}", query, ex.getMessage(), ex);
            }
        });
    }
}
