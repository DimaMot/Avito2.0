package ru.project.analytics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.project.analytics.dao.view.ItemViewStatsByCityRepository;
import ru.project.analytics.dao.view.ItemViewStatsRepository;
import ru.project.analytics.dao.view.ItemViewsDailyRepository;
import ru.project.analytics.entity.view.ItemViewStatsByCityEntity;
import ru.project.analytics.entity.view.ItemViewStatsEntity;
import ru.project.analytics.entity.view.ItemViewsDailyEntity;
import ru.project.commonlib.kafka.ItemViewEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewAnalyticsService {
    private static final int LAST_7_DAYS = 7;
    private final ItemViewStatsRepository statsRepository;
    private final ItemViewsDailyRepository dailyRepository;
    private final ItemViewStatsByCityRepository cityRepository;

    @Transactional
    public void registerView(List<ItemViewEvent> events) {
        if (events.isEmpty()) {
            return;
        }
        Map<Long, List<ItemViewEvent>> map = events.stream()
                .collect(Collectors.groupingBy(ItemViewEvent::itemId));

        List<ItemViewStatsEntity> statsEntities = new ArrayList<>();
        List<ItemViewsDailyEntity> dailyEntities = new ArrayList<>();
        List<ItemViewStatsByCityEntity> cityEntities = new ArrayList<>();

        Instant sevenDaysAgo = Instant.now().minus(LAST_7_DAYS, ChronoUnit.DAYS);

        for (Map.Entry<Long, List<ItemViewEvent>> entry : map.entrySet()) {
            Long itemId = entry.getKey();
            List<ItemViewEvent> itemViewEvents = entry.getValue();

            statsEntities.add(processStatAnalytic(itemId, itemViewEvents, sevenDaysAgo));
            cityEntities.addAll(processCityAnalytic(itemId, itemViewEvents));
            dailyEntities.addAll(processDayAnalytic(itemId, itemViewEvents));
        }
        if (!statsEntities.isEmpty())
            statsRepository.upsertBatch(statsEntities);
        if (!dailyEntities.isEmpty())
            dailyRepository.upsertBatch(dailyEntities);
        if(!cityEntities.isEmpty())
            cityRepository.upsertBatch(cityEntities);
    }

    private ItemViewStatsEntity processStatAnalytic(Long itemId, List<ItemViewEvent> itemViewEvents, Instant sevenDaysAgo) {
        log.info("Аналитика - статистика");
        long totalViews = itemViewEvents.size();

        long unique = itemViewEvents.stream()
                .filter(Objects::nonNull)
                .map(ItemViewEvent::userId)
                .distinct()
                .count();

        long sevenDays = itemViewEvents.stream()
                .map(ItemViewEvent::timestamp)
                .filter(ts -> ts.isAfter(sevenDaysAgo))
                .count();

        Instant lastViewedAt = itemViewEvents.stream()
                .map(ItemViewEvent::timestamp)
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(Instant.now());

        return ItemViewStatsEntity.builder()
                .itemId(itemId)
                .uniqueViews(unique)
                .totalViews(totalViews)
                .lastViewedAt(lastViewedAt)
                .viewsLastSevenDays(sevenDays)
                .build();
    }

    private List<ItemViewStatsByCityEntity> processCityAnalytic(Long itemId, List<ItemViewEvent> itemViewEvents) {
        log.info("Аналитика - город");
        Map<String, List<ItemViewEvent>> cities = itemViewEvents.stream()
                .filter(c -> c.city() != null)
                .collect(Collectors.groupingBy(e -> e.city().trim().toLowerCase()));

        List<ItemViewStatsByCityEntity> result = new ArrayList<>();

         for (Map.Entry<String, List<ItemViewEvent>> entry : cities.entrySet()) {
            String city = entry.getKey();
            List<ItemViewEvent> viewEvents = entry.getValue();

            long totalViews = viewEvents.size();

            Instant lastViewedAt = entry.getValue().stream()
                    .map(ItemViewEvent::timestamp)
                    .filter(Objects::nonNull)
                    .max(Instant::compareTo)
                    .orElse(Instant.now());

            result.add(
                    ItemViewStatsByCityEntity.builder()
                            .itemId(itemId)
                            .city(city)
                            .totalViews(totalViews)
                            .lastViewedAt(lastViewedAt)
                            .build()
            );
        }
         return result;
    }

    private List<ItemViewsDailyEntity> processDayAnalytic(Long itemId, List<ItemViewEvent> itemViewEvents) {
        log.info("Аналитика - день");
        Map<LocalDate, Long> byDay = itemViewEvents.stream()
                .collect(Collectors.groupingBy(e -> e.timestamp().atZone(ZoneOffset.UTC).toLocalDate(),
                        Collectors.counting())
                );
        List<ItemViewsDailyEntity> result = new ArrayList<>();

        for (Map.Entry<LocalDate, Long> entry : byDay.entrySet()) {
            result.add(
                    ItemViewsDailyEntity.builder()
                            .itemId(itemId)
                            .day(entry.getKey())
                            .views(entry.getValue())
                            .build()
            );
        }
        return result;
    }
}
