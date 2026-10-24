package ru.project.analytics.dao.view;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.project.analytics.dao.BaseRepository;
import ru.project.analytics.entity.view.ItemViewStatsEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Repository
public class ItemViewStatsRepository extends BaseRepository<ItemViewStatsEntity> {

    public ItemViewStatsRepository(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc, RowMapper<ItemViewStatsEntity> mapper) {
        super(jdbc, namedJdbc, mapper);
    }

    private static final String UPSERT_SQL = """
            INSERT INTO item_view_stats
                (item_id, total_views, unique_views, last_viewed_at, views_last_7_days)
            VALUES
                (?, ?, ?, ?, ?)
            ON CONFLICT (item_id)
            DO UPDATE SET
            total_views = item_view_stats.total_views + excluded.total_views,
            unique_views = item_view_stats.unique_views + excluded.unique_views,
            last_viewed_at = greatest(item_view_stats.last_viewed_at, excluded.last_viewed_at),
            views_last_7_days = item_view_stats.views_last_7_days + excluded.views_last_7_days
            """;

    private static final String FIND_TOP_ITEMS = """
            SELECT *
            FROM item_view_stats
            ORDER BY total_views DESC
            LIMIT :limit
            """;

    public void upsertBatch(List<ItemViewStatsEntity> stats) {
        List<Object[]> batchArgs = stats.stream()
                .map(s -> new Object[]{
                        s.itemId(),
                        s.totalViews(),
                        s.uniqueViews(),
                        OffsetDateTime.ofInstant(s.lastViewedAt(), ZoneOffset.UTC),
                        s.viewsLastSevenDays()
                })
                .toList();
        batchUpdate(UPSERT_SQL, batchArgs);
    }

    public List<ItemViewStatsEntity> findTopItems(int limit) {
        return findMany(FIND_TOP_ITEMS, new MapSqlParameterSource("limit", limit));
    }
}
