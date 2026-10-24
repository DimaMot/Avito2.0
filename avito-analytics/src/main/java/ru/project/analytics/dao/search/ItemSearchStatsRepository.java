package ru.project.analytics.dao.search;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.project.analytics.dao.BaseRepository;
import ru.project.analytics.entity.search.ItemSearchStatsEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Repository
public class ItemSearchStatsRepository extends BaseRepository<ItemSearchStatsEntity> {
    public ItemSearchStatsRepository(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc, RowMapper<ItemSearchStatsEntity> mapper) {
        super(jdbc, namedJdbc, mapper);
    }

    private static final String UPSERT_SQL = """
            INSERT INTO item_search_stats(query, total_views, results_count, unique_views, last_search_at)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (query)
            DO UPDATE SET
            unique_views = item_search_stats.unique_views + excluded.unique_views,
            total_views = item_search_stats.total_views + excluded.total_views,
            results_count = excluded.results_count,
            last_search_at = greatest(item_search_stats.last_search_at, excluded.last_search_at)
            """;

    public void upsertBatch(List<ItemSearchStatsEntity> stats) {
        List<Object[]> batchArgs = stats.stream()
                .map(data -> new Object[]{
                        data.query(),
                        data.totalViews(),
                        data.resultsCount(),
                        data.uniqueViews(),
                        OffsetDateTime.ofInstant(data.lastSearchAt(), ZoneOffset.UTC)
                })
                .toList();

        jdbc.batchUpdate(UPSERT_SQL, batchArgs);
    }
}
