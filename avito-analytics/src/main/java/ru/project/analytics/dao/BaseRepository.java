package ru.project.analytics.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class BaseRepository<T> {
    protected final JdbcTemplate jdbc;
    protected final NamedParameterJdbcTemplate namedJdbc;
    protected final RowMapper<T> mapper;

    protected int update(String query, Object... params) {
        return jdbc.update(query, params);
    }

    protected long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int ind = 0; ind < params.length; ind++) {
                preparedStatement.setObject(ind + 1, params[ind]);
            }
            return preparedStatement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Не удалось получить сгенерированный ID");
        }

        return key.longValue();
    }

    protected boolean delete(String query, Object... id) {
        return jdbc.update(query, id) > 0;
    }

    protected Optional<T> findOne(String query, Object... params) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(query, mapper, params));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    protected List<T> findMany(String query, MapSqlParameterSource params) {
        return namedJdbc.query(query, params, mapper);
    }

    protected void batchUpdate(String query, List<Object[]> batchArgs) {
        if (batchArgs == null || batchArgs.isEmpty()) {
            return;
        }
        jdbc.batchUpdate(query, batchArgs);
    }
}
