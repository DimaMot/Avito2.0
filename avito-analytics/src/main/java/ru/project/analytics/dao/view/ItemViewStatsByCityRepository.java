package ru.project.analytics.dao.view;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.project.analytics.dao.BaseRepository;
import ru.project.analytics.entity.view.ItemViewStatsByCityEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Repository
public class ItemViewStatsByCityRepository extends BaseRepository<ItemViewStatsByCityEntity> {

    public ItemViewStatsByCityRepository(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc, RowMapper<ItemViewStatsByCityEntity> mapper) {
        super(jdbc, namedJdbc, mapper);
    }

    private static final String UPSERT_SQL = """
            INSERT INTO item_view_stats_by_city
                (item_id, city, total_views, last_viewed_at)
            VALUES
                (?, ?, ?, ?)
            ON CONFLICT (item_id, city)
            DO UPDATE SET
            total_views = item_view_stats_by_city.total_views + excluded.total_views,
            last_viewed_at = greatest(item_view_stats_by_city.last_viewed_at, excluded.last_viewed_at)
            """;

    private static final String STATIC_BY_ITEM = """
            SELECT *
            FROM item_view_stats_by_city
            WHERE item_id IN (:itemIds)
            ORDER BY total_views DESC
            """;

    private static final String STATIC_BY_CITY = """
            SELECT *
            FROM item_view_stats_by_city
            WHERE city IN (:cities)
            ORDER BY total_views DESC
            """;

    public void upsertBatch(List<ItemViewStatsByCityEntity> stats) {
        List<Object[]> batchArgs = stats.stream()
                .map(s -> new Object[]{
                        s.itemId(),
                        s.city(),
                        s.totalViews(),
                        OffsetDateTime.ofInstant(s.lastViewedAt(), ZoneOffset.UTC)
                })
                .toList();
        batchUpdate(UPSERT_SQL, batchArgs);
    }

    public List<ItemViewStatsByCityEntity> staticByItem(List<Long> itemIds) {
        return findMany(STATIC_BY_ITEM, new MapSqlParameterSource("itemIds", itemIds));
    }

    public List<ItemViewStatsByCityEntity> staticByCity(List<String> cities) {
        return findMany(STATIC_BY_CITY, new MapSqlParameterSource("cities", cities));
    }
}
