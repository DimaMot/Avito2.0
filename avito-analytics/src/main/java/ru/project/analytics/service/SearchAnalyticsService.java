package ru.project.analytics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.project.analytics.dao.search.ItemSearchStatsByCityRepository;
import ru.project.analytics.dao.search.ItemSearchStatsRepository;
import ru.project.analytics.entity.search.ItemSearchStatsByCityEntity;
import ru.project.analytics.entity.search.ItemSearchStatsEntity;
import ru.project.commonlib.kafka.ItemSearchEvent;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchAnalyticsService {
    private final ItemSearchStatsByCityRepository cityRepository;
    private final ItemSearchStatsRepository statsRepository;

    @Transactional
    public void registerSearch(List<ItemSearchEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        List<ItemSearchStatsEntity> statsEntities = new ArrayList<>();
        List<ItemSearchStatsByCityEntity> cityEntities = new ArrayList<>();

        Map<String, List<ItemSearchEvent>> groupByQuery = events.stream()
                .collect(Collectors.groupingBy(ItemSearchEvent::queryText));

        for (Map.Entry<String, List<ItemSearchEvent>> entry : groupByQuery.entrySet()) {
            String query = entry.getKey();
            List<ItemSearchEvent> data = entry.getValue();

            statsEntities.add(statsAnalytic(query, data));
            cityEntities.addAll(cityAnalytics(query, data));
        }

        saveData(statsEntities, cityEntities);
    }

    private void saveData(List<ItemSearchStatsEntity> statsEntities, List<ItemSearchStatsByCityEntity> cityEntities) {
        statsRepository.upsertBatch(statsEntities);
        cityRepository.upsertBatch(cityEntities);
    }

    private ItemSearchStatsEntity statsAnalytic(String query, List<ItemSearchEvent> data) {

        long totalViews = data.size();

        ItemSearchEvent itemSearchEvent = data.stream()
                .filter(e -> e.timestamp() != null)
                .max(Comparator.comparing(ItemSearchEvent::timestamp))
                .orElse(data.getFirst());

        int resultCount = itemSearchEvent.resultsCount();

        long uniqueViews = data.stream()
                .map(item -> {
                    if (item.userId() == null) {
                        return 0;
                    }
                    return item.userId();
                })
                .distinct()
                .count();

        Instant lastSearchAt = itemSearchEvent.timestamp();

        return ItemSearchStatsEntity.builder()
                .query(query)
                .totalViews(totalViews)
                .resultsCount(resultCount)
                .uniqueViews(uniqueViews)
                .lastSearchAt(lastSearchAt)
                .build();
    }

    private List<ItemSearchStatsByCityEntity> cityAnalytics(String query, List<ItemSearchEvent> data) {
        List<ItemSearchStatsByCityEntity> result = new ArrayList<>();

        ItemSearchEvent itemSearchEvent = data.stream()
                .filter(e -> e.timestamp() != null)
                .max(Comparator.comparing(ItemSearchEvent::timestamp))
                .orElse(data.getFirst());

        int resultCount = itemSearchEvent.resultsCount();

        Map<String, List<ItemSearchEvent>> mapForCities = data.stream()
                .collect(Collectors.groupingBy(ItemSearchEvent::city));

        for (Map.Entry<String, List<ItemSearchEvent>> listEntry : mapForCities.entrySet()) {
            String city = listEntry.getKey();
            List<ItemSearchEvent> eventsList = listEntry.getValue();

            Instant lastSearchAt = eventsList.stream()
                    .map(ItemSearchEvent::timestamp)
                    .filter(Objects::nonNull)
                    .max(Instant::compareTo)
                    .orElse(Instant.now());

            result.add(
                    ItemSearchStatsByCityEntity.builder()
                            .query(query)
                            .resultsCount(resultCount)
                            .city(city)
                            .totalViews(eventsList.size())
                            .lastSearchAt(lastSearchAt)
                            .build()
            );
        }

        return result;
    }
}
