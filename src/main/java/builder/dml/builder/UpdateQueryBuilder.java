package builder.dml.builder;

import builder.dml.EntityData;

public class UpdateQueryBuilder {

    private final static String UPDATE_BY_ID_QUERY = "UPDATE {tableName} SET {columnDefinitions} WHERE {entityPkName} = {values};";
    private final static String TABLE_NAME = "{tableName}";
    private final static String VALUES = "{values}";
    private final static String ENTITY_PK_NAME = "{entityPkName}";
    private final static String COLUMN_DEFINITIONS = "{columnDefinitions}";

    public String buildQuery(EntityData EntityData) {
        return updateByIdQuery(EntityData);
    }

    //update 쿼리를 생성한다.
    private String updateByIdQuery(EntityData EntityData) {
        // 최종 SQL 쿼리 생성
        return UPDATE_BY_ID_QUERY.replace(TABLE_NAME, EntityData.getTableName())
                .replace(COLUMN_DEFINITIONS, EntityData.getColumnDefinitions())
                .replace(ENTITY_PK_NAME, EntityData.getPkNm())
                .replace(VALUES, String.valueOf(EntityData.wrapString()));
    }
}
