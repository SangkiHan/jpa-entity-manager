package persistence;

public class EntityEntry {

    private final EntityStatus entityStatus;

    public EntityEntry(EntityStatus entityStatus) {
        this.entityStatus = entityStatus;
    }

    public boolean equalsEntityStatus(EntityStatus entityStatus) {
        return this.entityStatus.equals(entityStatus);
    }
}
