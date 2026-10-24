package ru.project.analytics.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.project.analytics.service.SearchAnalyticsServer;
import ru.project.commonlib.kafka.ItemSearchEvent;

import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
@Component
@Slf4j
public class itemSearchConsumer {
    private final SearchAnalyticsServer analyticsServer;

    @KafkaListener(
        topics = ("${spring.kafka.topic.search}"),
        containerFactory = "itemSearchEventListener"
    )
    public void consumerItemSearch(ConsumerRecords<String, ItemSearchEvent> records) {
        if (records.isEmpty()) {
            return;
        }
        List<ItemSearchEvent> batchList = new ArrayList<>(records.count());
        log.info("количество записей - {}", records.count());
        for (ConsumerRecord<String, ItemSearchEvent> record : records) {
            if (record.value() == null || record.value().queryText().isEmpty()) {
                log.warn("Полученный ивент пуст(");
                continue;
            }
            log.info("Получен ивент: topic={}, partition={}, event={}",
                    record.topic(), record.partition(), record.value());
            batchList.add(record.value());
        }

        analyticsServer.registerSearch(batchList);
    }
}
