package gukjin.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class CheckedTest {


    @Test
    void checked_catch(){
        Service service = new Service();
        service.callCatch();
    }
    @Test
    void checked_throw(){
        Service service = new Service();
        Assertions.assertThatThrownBy(()->service.callThrow())
                .isInstanceOf(MyCheckedException.class);
    }


    /**
     * Exception을 상속받은 예외는 Checked Exception
     */
    static class MyCheckedException extends Exception{
        public MyCheckedException(String message) {
            super(message);
        }
    }

    /**
     * checked 예외는 잡거나 던지거나 둘 중 하나를 필수적으로 해야 함
     */
    static class Service{
        Repository repository = new Repository();
        /**
         * 예외를 잡아 처리하는 코드
         */
        public void callCatch(){
            try {
                repository.call();
            } catch (MyCheckedException e) {
                // 예외 처리 로직
                log.info("예외 처리, message = {}", e.getMessage(), e);
            }
        }

        /**
         * 예외를 잡지 않고 밖으로 던짐
         * 체크 예외는 잡지 않고 던지려면 메서드에 필수적으로 표시
         * @throws MyCheckedException
         */
        public void callThrow() throws MyCheckedException {
            repository.call();
        }
    }

    static class Repository{
        public void call() throws MyCheckedException {
            throw new MyCheckedException("ex");
        }
    }
}
