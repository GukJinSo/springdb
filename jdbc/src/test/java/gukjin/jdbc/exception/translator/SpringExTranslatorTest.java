package gukjin.jdbc.exception.translator;

import gukjin.jdbc.connection.ConnectionConst;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static gukjin.jdbc.connection.ConnectionConst.*;

public class SpringExTranslatorTest {

    DataSource dataSource

    @BeforeEach
    public void init(){
     dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    }

    @Test
    void sqlBadGrammarTest(){
        String sql = "select from badGrammar";

        try{
            Connection con = dataSource.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.executeUpdate();
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
