package ru.project.analytics.dao.view.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.project.analytics.entity.view.ItemViewStatsByCityEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class ItemViewStatsByCityEntityMapper implements RowMapper<ItemViewStatsByCityEntity> {
    @Override
    public ItemViewStatsByCityEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp date = rs.getTimestamp("last_viewed_at");

        return ItemViewStatsByCityEntity.builder()
                .id(rs.getLong("id"))
                .itemId(rs.getLong("item_id"))
                .city(rs.getString("city"))
                .totalViews(rs.getLong("total_views"))
                .lastViewedAt(date != null ? date.toInstant() : null)
                .build();
    }
}
