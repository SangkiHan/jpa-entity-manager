package persistence;

import builder.dml.DMLBuilderData;
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
            DMLBuilderData persistDmlBuilderData = this.persistenceContext.findEntity(entityKey);
            return clazz.cast(persistDmlBuilderData.getEntityInstance());
        }

        T findObject = this.entityLoader.find(clazz, id);
        DMLBuilderData dmlBuilderData = DMLBuilderData.createDMLBuilderData(findObject);

        insertPersistenceContext(entityKey, dmlBuilderData);
        this.persistenceContext.insertEntityEntryMap(entityKey, EntityStatus.MANAGED);

        return findObject;
    }

    @Override
    public void persist(Object entityInstance) {
        DMLBuilderData dmlBuilderData = DMLBuilderData.createDMLBuilderData(entityInstance);
        this.entityPersister.persist(dmlBuilderData);
        EntityKey<?> entityKey = new EntityKey<>(dmlBuilderData.getId(), dmlBuilderData.getClazz());
        insertPersistenceContext(entityKey, dmlBuilderData);
        this.persistenceContext.insertEntityEntryMap(entityKey, EntityStatus.MANAGED);
    }

    @Override
    public void merge(Object entityInstance) {
        DMLBuilderData dmlBuilderData = DMLBuilderData.createDMLBuilderData(entityInstance);
        EntityKey<?> entityKey = new EntityKey<>(dmlBuilderData.getId(), dmlBuilderData.getClazz());

        EntityEntry entityEntry = this.persistenceContext.getEntityEntryMap(entityKey);

        if (entityEntry != null && entityEntry.checkEntityStatus(EntityStatus.MANAGED)) {
            DMLBuilderData diffBuilderData = checkDirtyCheck(dmlBuilderData);

            if (diffBuilderData.getColumns().isEmpty()) {
                return;
            }

            this.entityPersister.merge(checkDirtyCheck(dmlBuilderData));

            insertPersistenceContext(entityKey, dmlBuilderData);
        }
    }

    @Override
    public void remove(Object entityInstance) {
        DMLBuilderData dmlBuilderData = DMLBuilderData.createDMLBuilderData(entityInstance);
        this.entityPersister.remove(dmlBuilderData);
        this.persistenceContext.deleteEntity(new EntityKey<>(dmlBuilderData.getId(), entityInstance.getClass()));
        this.persistenceContext.insertEntityEntryMap(new EntityKey<>(dmlBuilderData.getId(), dmlBuilderData.getClazz()), EntityStatus.DELETED);
    }

    private DMLBuilderData checkDirtyCheck(DMLBuilderData entityBuilderData) {
        EntityKey<?> entityKey = new EntityKey<>(entityBuilderData.getId(), entityBuilderData.getClazz());

        DMLBuilderData snapshotDmlBuilderData = this.persistenceContext.getDatabaseSnapshot(entityKey);

        List<DMLColumnData> differentColumns = entityBuilderData.getDifferentColumns(snapshotDmlBuilderData);

        return entityBuilderData.changeColumns(differentColumns);
    }

    private void insertPersistenceContext(EntityKey<?> entityKey, DMLBuilderData dmlBuilderData) {
        this.persistenceContext.insertEntity(entityKey, dmlBuilderData);
        this.persistenceContext.insertDatabaseSnapshot(entityKey, dmlBuilderData);
    }
}
