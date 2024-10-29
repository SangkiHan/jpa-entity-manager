package persistence;

import builder.dml.EntityData;
import builder.dml.DMLColumnData;
import jdbc.JdbcTemplate;

import java.util.List;

public class EntityManagerImpl implements EntityManager {

    private final EntityLoader entityLoader;
    private final EntityPersister entityPersister;
    private final PersistenceContext persistenceContext;

    public EntityManagerImpl(JdbcTemplate jdbcTemplate) {
        this.entityLoader = new EntityLoader(jdbcTemplate);
        this.entityPersister = new EntityPersister(jdbcTemplate);
        this.persistenceContext = new PersistenceContextImpl();
    }

    public EntityManagerImpl(PersistenceContext persistenceContext, JdbcTemplate jdbcTemplate) {
        this.entityLoader = new EntityLoader(jdbcTemplate);
        this.entityPersister = new EntityPersister(jdbcTemplate);
        this.persistenceContext = persistenceContext;
    }

    @Override
    public <T> T find(Class<T> clazz, Object id) {
        EntityKey<T> entityKey = new EntityKey<>(id, clazz);
        EntityEntry entityEntry = this.persistenceContext.getEntityEntryMap(entityKey);

        if (entityEntry != null && entityEntry.checkEntityStatus(EntityStatus.MANAGED)) {
            EntityData persistEntityData = this.persistenceContext.findEntity(entityKey);
            return clazz.cast(persistEntityData.getEntityInstance());
        }

        T findObject = this.entityLoader.find(clazz, id);
        EntityData entityData = EntityData.createEntityData(findObject);

        insertPersistenceContext(entityKey, entityData);
        this.persistenceContext.insertEntityEntryMap(entityKey, EntityStatus.MANAGED);

        return findObject;
    }

    @Override
    public void persist(Object entityInstance) {
        EntityData entityData = EntityData.createEntityData(entityInstance);
        this.entityPersister.persist(entityData);
        EntityKey<?> entityKey = new EntityKey<>(entityData.getId(), entityData.getClazz());
        insertPersistenceContext(entityKey, entityData);
        this.persistenceContext.insertEntityEntryMap(entityKey, EntityStatus.MANAGED);
    }

    @Override
    public void merge(Object entityInstance) {
        EntityData entityData = EntityData.createEntityData(entityInstance);
        EntityKey<?> entityKey = new EntityKey<>(entityData.getId(), entityData.getClazz());

        EntityEntry entityEntry = this.persistenceContext.getEntityEntryMap(entityKey);

        if (entityEntry != null && entityEntry.checkEntityStatus(EntityStatus.MANAGED)) {
            EntityData diffBuilderData = checkDirtyCheck(entityData);

            if (diffBuilderData.getColumns().isEmpty()) {
                return;
            }

            this.entityPersister.merge(checkDirtyCheck(entityData));

            insertPersistenceContext(entityKey, entityData);
        }
    }

    @Override
    public void remove(Object entityInstance) {
        EntityData entityData = EntityData.createEntityData(entityInstance);
        this.entityPersister.remove(entityData);
        this.persistenceContext.deleteEntity(new EntityKey<>(entityData.getId(), entityData.getClazz()));
        this.persistenceContext.insertEntityEntryMap(new EntityKey<>(entityData.getId(), entityData.getClazz()), EntityStatus.DELETED);
    }

    private EntityData checkDirtyCheck(EntityData entityBuilderData) {
        EntityKey<?> entityKey = new EntityKey<>(entityBuilderData.getId(), entityBuilderData.getClazz());

        EntityData snapshotEntityData = this.persistenceContext.getDatabaseSnapshot(entityKey);

        List<DMLColumnData> differentColumns = entityBuilderData.getDifferentColumns(snapshotEntityData);

        return entityBuilderData.changeColumns(differentColumns);
    }

    private void insertPersistenceContext(EntityKey<?> entityKey, EntityData EntityData) {
        this.persistenceContext.insertEntity(entityKey, EntityData);
        this.persistenceContext.insertDatabaseSnapshot(entityKey, EntityData);
    }
}
