package ru.project.analytics.dao.search.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.project.analytics.entity.search.ItemSearchStatsByCityEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class ItemSearchStatsByCityEntityMapper implements RowMapper<ItemSearchStatsByCityEntity> {
    @Override
    public ItemSearchStatsByCityEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp data = rs.getTimestamp("last_search_at");
        return ItemSearchStatsByCityEntity
                .builder()
                .id(rs.getLong("id"))
                .query(rs.getString("query"))
                .resultsCount(rs.getInt("results_count"))
                .city(rs.getString("city"))
                .totalViews(rs.getLong("total_views"))
                .lastSearchAt(data.toInstant())
                .build();
    }
}
