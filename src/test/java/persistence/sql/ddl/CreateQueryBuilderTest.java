package persistence.sql.ddl;

import entity.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateQueryBuilderTest {

    @DisplayName("Person객체를 통해 테이블 create 쿼리 생성")
    @Test
    void build() {
        //when
        CreateQueryBuilder builder = new CreateQueryBuilder();
        String query = builder.getQuery(Person.class);

        //then
        assertThat(query).isEqualToIgnoringWhitespace("" +
                "CREATE TABLE users (\n" +
                "        id BIGINT generated by default as identity,\n" +
                "        nick_name VARCHAR(50),\n" +
                "        old INT,\n" +
                "        email VARCHAR(50) NOT NULL,\n" +
                "        PRIMARY KEY(id)\n" +
                "    );");
    }
}