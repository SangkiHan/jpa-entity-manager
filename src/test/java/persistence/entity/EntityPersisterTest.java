package persistence.entity;

import database.DatabaseServer;
import database.H2;
import entity.Person;
import jdbc.JdbcTemplate;
import org.junit.jupiter.api.*;
import persistence.sql.Query;
import persistence.sql.dialect.h2.H2Dialect;

import static org.assertj.core.api.Assertions.assertThat;

class EntityPersisterTest {

    private static DatabaseServer server;
    private static JdbcTemplate jdbcTemplate;
    private static final Query QUERY = new Query(new H2Dialect());

    @BeforeAll
    static void beforeAll() throws Exception {
        try {
            server = new H2();
            server.start();
            jdbcTemplate = new JdbcTemplate(server.getConnection());
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @BeforeEach
    void beforeEach() {
        jdbcTemplate.execute("create table users (id bigint generated by default as identity, nick_name varchar(255), old integer, email varchar(255) not null, primary key (id))");
        jdbcTemplate.execute("insert into users (id, nick_name, old, email) values (default, 'test1', 10, 'test1@gmail.com')");
    }

    @AfterEach
    void afterEach() {
        jdbcTemplate.execute("drop table if exists users CASCADE");
    }

    @DisplayName("EntityPersister#update를 통해 Entity를 수정(update)한다.")
    @Test
    void updateTest() {
        // given
        EntityPersister entityPersister = new EntityPersister(QUERY, jdbcTemplate);
        SimpleEntityManager entityManager = new SimpleEntityManager(QUERY, jdbcTemplate, entityPersister);
        Person testEntity = new Person(1L, "test2", 11, "test2@gmail.com", 0);

        // when
        entityPersister.update(testEntity);
        Person found = entityManager.find(Person.class, 1L);

        // then
        assertThat(found.equals(testEntity)).isTrue();
    }

    @DisplayName("EntityPersister#insert를 통해 Entity를 저장(insert)한다.")
    @Test
    void insertTest() {
        // given
        EntityPersister entityPersister = new EntityPersister(QUERY, jdbcTemplate);
        SimpleEntityManager entityManager = new SimpleEntityManager(QUERY, jdbcTemplate, entityPersister);
        Person testEntity = new Person(2L, "test2", 11, "test2@gmail.com", 0);

        // when
        entityPersister.insert(testEntity);
        Person found = entityManager.find(Person.class, 2L);

        // then
        assertThat(found.equals(testEntity)).isTrue();
    }

    @DisplayName("EntityPersister#delete를 통해 Entity를 삭제(delete)한다.")
    @Test
    void deleteTest() {
        // given
        EntityPersister entityPersister = new EntityPersister(QUERY, jdbcTemplate);
        EntityManager entityManager = new SimpleEntityManager(QUERY, jdbcTemplate, entityPersister);
        Person testEntity = new Person(1L, "test1", 10, "test1@gmail.com", 0);

        // when
        entityPersister.delete(testEntity);

        // then
        assertThat(entityManager.find(Person.class, 1L)).isNull();
    }

}
