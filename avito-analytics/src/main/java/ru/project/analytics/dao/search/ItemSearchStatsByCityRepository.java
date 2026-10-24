package ru.project.analytics.dao.search;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.project.analytics.dao.BaseRepository;
import ru.project.analytics.entity.search.ItemSearchStatsByCityEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Repository
public class ItemSearchStatsByCityRepository extends BaseRepository<ItemSearchStatsByCityEntity> {
    public ItemSearchStatsByCityRepository(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc, RowMapper<ItemSearchStatsByCityEntity> mapper) {
        super(jdbc, namedJdbc, mapper);
    }

    private static final String UPSERT_SQL = """
            INSERT INTO item_search_city(query, results_count, city, total_views, last_search_at)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (query, city)
            DO UPDATE SET
            results_count = excluded.results_count,
            total_views = item_search_city.total_views + excluded.total_views,
            last_search_at = greatest(item_search_city.last_search_at, excluded.last_search_at)
            """;

    public void upsertBatch(List<ItemSearchStatsByCityEntity> stats) {
        List<Object[]> batchArgs = stats.stream()
                .map(data -> new Object[]{
                        data.query(),
                        data.resultsCount(),
                        data.city(),
                        data.totalViews(),
                        OffsetDateTime.ofInstant(data.lastSearchAt(), ZoneOffset.UTC),
                })
                .toList();
        jdbc.batchUpdate(UPSERT_SQL, batchArgs);
    }
}
