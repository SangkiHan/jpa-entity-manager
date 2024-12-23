package persistence;

import builder.dml.EntityData;
import builder.dml.builder.SelectByIdQueryBuilder;
import jdbc.EntityMapper;
import jdbc.JdbcTemplate;

public class EntityLoader {

    private final SelectByIdQueryBuilder selectByIdQueryBuilder = new SelectByIdQueryBuilder();
    private final JdbcTemplate jdbcTemplate;

    public EntityLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //데이터를 조회한다.
    public <T> T find(Class<T> clazz, Object id) {
        return jdbcTemplate.queryForObject(selectByIdQueryBuilder.buildQuery(EntityData.createEntityData(clazz, id)), resultSet -> EntityMapper.mapRow(resultSet, clazz));
    }

}
