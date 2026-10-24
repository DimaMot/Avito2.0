package ru.project.analytics.dao.view.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.project.analytics.entity.view.ItemViewStatsEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class ItemViewStatsEntityMapper implements RowMapper<ItemViewStatsEntity> {
    @Override
    public ItemViewStatsEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp date = rs.getTimestamp("last_viewed_at");

        return ItemViewStatsEntity.builder()
                .itemId(rs.getLong("item_id"))
                .totalViews(rs.getLong("total_views"))
                .lastViewedAt(date.toInstant())
                .uniqueViews(rs.getLong("unique_views"))
                .viewsLastSevenDays(rs.getLong("views_last_7_days"))
                .build();
    }
}
