package gukjin.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

@Slf4j
public class UncheckedAppTest {

    @Test
    void checked(){
        Controller controller = new Controller();
        Assertions.assertThatThrownBy(()->controller.request())
                .isInstanceOf(RuntimeConnectException.class);
    }

    static class Controller{
        Service service = new Service();

        public void request() {
            service.logic();
        }
    }

    static class Service{
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() {
            repository.call();
            networkClient.call();
        }
    }
    static class NetworkClient{

        public void call(){
            try{
                throw new ConnectException();
            }catch(ConnectException e){
                throw new RuntimeConnectException(e);
            }
        }
    }
    static class Repository{
        public void call() {
            try {
                runSQL();
            } catch (SQLException e) {
                throw new RuntimeSQLException(e);
            }

        }
        private void runSQL() throws SQLException {
            throw new SQLException();
        }
    }


    static class RuntimeConnectException extends RuntimeException{
        public RuntimeConnectException(Throwable cause) {
            super(cause);
        }
    }
    static class RuntimeSQLException extends RuntimeException{
        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }
    }
}
