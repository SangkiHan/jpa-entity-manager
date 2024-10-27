package persistence;

import builder.dml.DMLBuilderData;
import entity.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/*
- 영속성 컨텍스트에 Entity객체를 저장 후 저장되어있는 Entity 객체를 가져온다.
- 영속성 컨텍스트에 저장되어있는 Entity 객체를 제거한다.
- 영속성 컨텍스트에서 스냅샷을 생성한다.
- 영속성 컨텍스트에서 스냅샷을 가져온다.
- 영속성 컨텍스트에서 EntityStatus를 저장 후 저장되어있는 EntityEntry 객체를 가져온다.
*/
class PersistenceContextImplTest {

    @DisplayName("영속성 컨텍스트에 Entity객체를 저장 후 저장되어있는 Entity 객체를 가져온다.")
    @Test
    void insertFindTest() {
        PersistenceContextImpl persistenceContext = new PersistenceContextImpl();
        Person person = createPerson(1);
        EntityKey<Person> EntityKey = new EntityKey<>(1, Person.class);

        DMLBuilderData dmlBuilderData = DMLBuilderData.createDMLBuilderData(person);

        persistenceContext.insertEntity(EntityKey, dmlBuilderData);

        assertThat(persistenceContext.findEntity(new EntityKey<>(1, Person.class)).getEntityInstance())
                .extracting("id", "name", "age", "email")
                .contains(1L, "test1", 29, "test@test.com");
    }

    @DisplayName("영속성 컨텍스트에 저장되어있는 Entity 객체를 제거한다.")
    @Test
    void removeTest() {
        PersistenceContextImpl persistenceContext = new PersistenceContextImpl();
        Person person = createPerson(1);
        DMLBuilderData dmlBuilderData = DMLBuilderData.createDMLBuilderData(person);
        IntStream.range(1,3).forEach(i -> persistenceContext.insertEntity(new EntityKey<>(i, Person.class), dmlBuilderData));

        persistenceContext.deleteEntity(new EntityKey<>(2, Person.class));

        assertThat(persistenceContext.findEntity(new EntityKey<>(2, Person.class))).isNull();
    }

    @DisplayName("영속성 컨텍스트에서 스냅샷을 생성한다.")
    @Test
    void addDatabaseSnapshotTest() {
        PersistenceContextImpl persistenceContext = new PersistenceContextImpl();
        Person person = createPerson(1);
        DMLBuilderData dmlBuilderData = DMLBuilderData.createDMLBuilderData(person);
        IntStream.range(1,3).forEach(i -> persistenceContext.insertDatabaseSnapshot(new EntityKey<>(i, Person.class), dmlBuilderData));

        assertThat(persistenceContext.findEntity(new EntityKey<>(2, Person.class))).isNull();
    }

    @DisplayName("영속성 컨텍스트에서 스냅샷을 가져온다.")
    @Test
    void getDatabaseSnapshotTest() {
        PersistenceContextImpl persistenceContext = new PersistenceContextImpl();
        Person person = createPerson(1);
        DMLBuilderData dmlBuilderData = DMLBuilderData.createDMLBuilderData(person);
        persistenceContext.insertDatabaseSnapshot(new EntityKey<>(person.getId(), Person.class), dmlBuilderData);
        assertThat(persistenceContext.getDatabaseSnapshot(new EntityKey<>(person.getId(), Person.class)).getEntityInstance())
                .extracting("id", "name", "age", "email")
                .contains(1L, "test1", 29, "test@test.com");
    }

    @DisplayName("영속성 컨텍스트에서 EntityStatus를 저장 후 저장되어있는 EntityEntry 객체를 가져온다.")
    @Test
    void insertEntityEntryMapTest() {
        PersistenceContextImpl persistenceContext = new PersistenceContextImpl();
        Person person = createPerson(1);

        persistenceContext.insertEntityEntryMap(new EntityKey<>(person.getId(), Person.class), EntityStatus.MANAGED);
        assertThat(persistenceContext.getEntityEntryMap(new EntityKey<>(person.getId(), Person.class)))
                .extracting("entityStatus").isEqualTo(EntityStatus.MANAGED);
    }

    private Person createPerson(int i) {
        return new Person((long) i, "test" + i, 29, "test@test.com");
    }

}
