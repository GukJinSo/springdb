package gukjin.connection;

import gukjin.jdbc.connection.DbConnectionUtil;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

class DbConnectionUtilTest {

    @Test
    void connection(){
        Connection connection = DbConnectionUtil.getConnection();
        assertThat(connection).isNotNull();
    }

}