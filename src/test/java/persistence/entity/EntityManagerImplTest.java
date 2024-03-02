package persistence.entity;

import database.DatabaseServer;
import database.H2;
import jdbc.JdbcTemplate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.domain.Person;
import persistence.sql.ddl.builder.CreateQueryBuilder;
import persistence.sql.ddl.builder.DropQueryBuilder;
import persistence.sql.ddl.builder.QueryBuilder;
import persistence.sql.ddl.dialect.H2Dialect;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityManagerImplTest {

    private static DatabaseServer server;
    private JdbcTemplate jdbcTemplate;
    private EntityManager<Person> entityManager;

    @BeforeAll
    static void init() throws SQLException {
        server = new H2();
        server.start();
    }

    @AfterAll
    static void shutDown() {
        server.stop();
    }

    @BeforeEach
    void setup() throws SQLException {
        jdbcTemplate = new JdbcTemplate(server.getConnection());
        entityManager = new EntityManagerImpl<>(jdbcTemplate);

        QueryBuilder createQueryBuilder = new CreateQueryBuilder(new H2Dialect());
        jdbcTemplate.execute(createQueryBuilder.generateSQL(Person.class));
    }

    @AfterEach
    void clean() {
        String sql = new DropQueryBuilder(new H2Dialect()).generateSQL(Person.class);
        jdbcTemplate.execute(sql);
    }

    @DisplayName("find/id로 조회/성공")
    @Test
    void findOne() {
        Person person = new Person("hoon25", 20, "hoon25@gmail.com");
        entityManager.persist(person);

        Person findPerson = entityManager.find(Person.class, 1L);

        assertThat(person.getName()).isEqualTo(findPerson.getName());
    }

    @DisplayName("find/id로 조회/실패")
    @Test
    void findOneFail() {
        assertThatThrownBy(() -> entityManager.find(Person.class, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("persist/entity 저장/저장 성공")
    @Test
    void persistInsert() {
        Person person = new Person("hoon25", 20, "hoon25@gmail.com");

        entityManager.persist(person);

        Person findPerson = entityManager.find(Person.class, 1L);
        assertThat(person.getName()).isEqualTo(findPerson.getName());
    }

    @DisplayName("persist/entity 수정/수정 성공")
    @Test
    void persistUpdate() {
        Person person = new Person("hoon25", 20, "hoon25@gmail.com");
        entityManager.persist(person);
        Person findPerson = entityManager.find(Person.class, 1L);
        findPerson.changeName("hoon");

        entityManager.persist(findPerson);

        Person updatePerson = entityManager.find(Person.class, 1L);
        assertThat(updatePerson.getName()).isEqualTo("hoon");
    }


    @DisplayName("remove/entity 삭제/삭제 성공")
    @Test
    void remove() {
        Person person = new Person("hoon25", 20, "hoon25@gmail.com");
        entityManager.persist(person);

        Person findPerson = entityManager.find(Person.class, 1L);
        entityManager.remove(findPerson);

        assertThatThrownBy(() -> entityManager.find(Person.class, 1L))
                .isInstanceOf(RuntimeException.class);
    }
}
