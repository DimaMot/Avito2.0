package ru.project.analytics.dao.view;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.project.analytics.dao.BaseRepository;
import ru.project.analytics.entity.view.ItemViewsDailyEntity;

import java.time.LocalDate;
import java.util.List;

@Repository
public class ItemViewsDailyRepository extends BaseRepository<ItemViewsDailyEntity> {

    public ItemViewsDailyRepository(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc, RowMapper<ItemViewsDailyEntity> mapper) {
        super(jdbc, namedJdbc, mapper);
    }

    private static final String UPSERT_SQL = """
            INSERT INTO item_views_daily(item_id, day, views)
            VALUES (?, ?, ?)
            ON CONFLICT (item_id, day)
            DO UPDATE SET
            views = item_views_daily.views + excluded.views
            """;

    private static final String STATIC_FROM_TO = """
            SELECT *
            FROM item_views_daily
            WHERE item_id IN (:itemIds)
            AND day BETWEEN :from AND :to
            """;

    public void upsertBatch(List<ItemViewsDailyEntity> stats) {
        List<Object[]> batchArgs = stats.stream()
                .map(s -> new Object[]{
                        s.itemId(),
                        s.day(),
                        s.views()
                })
                .toList();
        batchUpdate(UPSERT_SQL, batchArgs);
    }

    public List<ItemViewsDailyEntity> staticFromTo(List<Long> itemIds, LocalDate from, LocalDate to) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("itemIds", itemIds)
                .addValue("from", from)
                .addValue("to", to);
        return findMany(STATIC_FROM_TO, params);
    }
}
