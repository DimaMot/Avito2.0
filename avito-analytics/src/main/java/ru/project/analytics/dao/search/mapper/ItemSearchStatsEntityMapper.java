package ru.project.analytics.dao.search.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.project.analytics.entity.search.ItemSearchStatsEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class ItemSearchStatsEntityMapper implements RowMapper<ItemSearchStatsEntity> {
    @Override
    public ItemSearchStatsEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp data = rs.getTimestamp("last_search_at");
        return ItemSearchStatsEntity.builder()
                .id(rs.getLong("id"))
                .query(rs.getString("query"))
                .totalViews(rs.getLong("total_views"))
                .resultsCount(rs.getInt("results_count"))
                .uniqueViews(rs.getLong("unique_views"))
                .lastSearchAt(data.toInstant())
                .build();
    }
}
