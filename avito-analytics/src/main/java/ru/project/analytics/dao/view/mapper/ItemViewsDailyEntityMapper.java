package ru.project.analytics.dao.view.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.project.analytics.entity.view.ItemViewsDailyEntity;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ItemViewsDailyEntityMapper implements RowMapper<ItemViewsDailyEntity> {
    @Override
    public ItemViewsDailyEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        Date date = rs.getDate("day");

        return ItemViewsDailyEntity.builder()
                .id(rs.getLong("id"))
                .itemId(rs.getLong("item_id"))
                .day(date.toLocalDate())
                .views(rs.getLong("views"))
                .build();
    }
}
