package ru.project.analytics.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.project.analytics.service.ViewAnalyticsService;
import ru.project.commonlib.kafka.ItemViewEvent;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemViewConsumer {
    private final ViewAnalyticsService viewAnalyticsService;

    @KafkaListener(topics = ("${spring.kafka.topic.view}"),
            containerFactory = "itemViewEventListener"
    )
    public void consumeItemView(ConsumerRecords<String, ItemViewEvent> records) {
        if (records.isEmpty()) {
            return;
        }
        List<ItemViewEvent> batchList = new ArrayList<>(records.count());
        log.info("количество записей - {}", records.count());
        for (ConsumerRecord<String, ItemViewEvent> record : records) {
            if (record.value() == null || record.value().itemId() == null) {
                log.warn("Полученный ивент пуст(");
                continue;
            }

            log.info("Получен ивент: topic={}, partition={}, event={}",
                    record.topic(), record.partition(), record.value());

            batchList.add(record.value());
        }
        viewAnalyticsService.registerView(batchList);
    }
}
