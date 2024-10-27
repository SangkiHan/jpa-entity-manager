package persistence;

import builder.dml.DMLBuilderData;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class PersistenceContextImpl implements PersistenceContext {

    private final Map<EntityKey<?>, DMLBuilderData> entityMap = new HashMap<>();
    private final Map<EntityKey<?>, DMLBuilderData> snapShotMap = new HashMap<>();
    private final Map<EntityKey<?>, EntityEntry> entityEntryMap = new HashMap<>();

    @Override
    public DMLBuilderData findEntity(EntityKey<?> entityKey) {
        return entityMap.get(entityKey);
    }

    @Override
    public void insertEntity(EntityKey<?> entityKey, DMLBuilderData dmlBuilderData) {
        this.entityMap.put(entityKey, dmlBuilderData);
    }

    @Override
    public void deleteEntity(EntityKey<?> entityKey) {
        this.entityMap.remove(entityKey);
    }

    @Override
    public void insertDatabaseSnapshot(EntityKey<?> entityKey, DMLBuilderData dmlBuilderData) {
        this.snapShotMap.put(entityKey, dmlBuilderData);
    }

    @Override
    public DMLBuilderData getDatabaseSnapshot(EntityKey<?> entityKey) {
        return this.snapShotMap.get(entityKey);
    }

    @Override
    public void insertEntityEntryMap(EntityKey<?> entityKey, EntityStatus entityStatus) {
        EntityEntry entityEntry = new EntityEntry(entityStatus);
        this.entityEntryMap.put(entityKey, entityEntry);
    }

    @Override
    public EntityEntry getEntityEntryMap(EntityKey<?> entityKey) {
        return this.entityEntryMap.get(entityKey);
    }

}
