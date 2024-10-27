package persistence;

import builder.dml.DMLBuilderData;

public interface PersistenceContext {

    DMLBuilderData findEntity(EntityKey<?> entityKey);

    void insertEntity(EntityKey<?> entityKey, DMLBuilderData dmlBuilderData);

    void deleteEntity(EntityKey<?> entityKey);

    void insertDatabaseSnapshot(EntityKey<?> entityKey, DMLBuilderData dmlBuilderData);

    DMLBuilderData getDatabaseSnapshot(EntityKey<?> entityKey);

    void insertEntityEntryMap(EntityKey<?> entityKey, EntityStatus entityStatus);

    EntityEntry getEntityEntryMap(EntityKey<?> entityKey);

}
