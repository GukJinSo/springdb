package gukjin.jdbc.exception.translator;

import gukjin.jdbc.connection.ConnectionConst;
import gukjin.jdbc.domain.Member;
import gukjin.jdbc.repository.ex.MyDbException;
import gukjin.jdbc.repository.ex.MyDuplicateKeyException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import static gukjin.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ExTranslatorV1Test {

    Repository repository;
    Service service;

    @BeforeEach
    @DisplayName("예외 전환 처리 테스트 - 세팅")
    void beforeEach(){
        DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        repository = new Repository(dataSource);
        service = new Service(repository);
    }

    @Test
    @DisplayName("예외 전환 처리 테스트 - 실행")
    void test(){
        service.create("myId");
        service.create("myId");
    }

    @RequiredArgsConstructor
    static class Service{
        private final Repository repository;

        public void create(String memberId){
            try{
                repository.save(new Member(memberId, 0));
                log.info("saveId = {}", memberId);
            } catch (MyDuplicateKeyException e){
                log.warn("키 중복 발생, 복구 시도");
                String retryId = randomIdGenerate(memberId);
                log.info("retry Id = {}", retryId);
                repository.save(new Member(retryId, 0));
            } catch (MyDbException e){
                log.warn("데이터 접근 계층 예외", e);
                throw e;
            } // 굳이 여기서 로그를 안 남겨도 된다. 공통 예외 처리하는 곳에서 log를 다 찍기 때문
        }

        private String randomIdGenerate(String memberId){
            return memberId + new Random().nextInt(10000);
        }
    }



    @RequiredArgsConstructor
    static class Repository{
        private final DataSource dataSource;

        public Member save(Member member){
            String sql = "insert into member(member_id, money) values (?,?)";
            Connection con = null;
            PreparedStatement pstmt = null;

            try{
                con = dataSource.getConnection();
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, member.getMemberId());
                pstmt.setInt(2, member.getMoney());
                pstmt.executeUpdate();
                return member;
            }catch(SQLException e){
                if (e.getErrorCode() == 23505){
                    throw new MyDuplicateKeyException(e);
                }
                throw new MyDbException(e);
            } finally {
                JdbcUtils.closeStatement(pstmt);
                JdbcUtils.closeConnection(con);
            }
        }
    }
}
