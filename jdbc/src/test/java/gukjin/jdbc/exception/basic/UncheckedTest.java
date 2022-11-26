package gukjin.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class UncheckedTest {

    @Test
    public void unchecked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    public void unchecked_throw() {
        Service service = new Service();
        Assertions.assertThatThrownBy(()-> service.callThrow())
                .isInstanceOf(RuntimeException.class);

    }

    /**
     * RuntimeException을 상속받는 예외면 언체크 예외
     */
    static class MyUncheckedException extends RuntimeException{
        public MyUncheckedException(String message) {
            super(message);
        }
    }

    static class Service{
        Repository repository = new Repository();

        /**
         * 필요한 경우 예외를 잡아 처리하면 된다
         */
        public void callCatch(){
            try{
                repository.call();
            } catch (MyUncheckedException e){
                log.error("error message = {}", e.getMessage(), e);
            }
        }

        public void callThrow(){
            repository.call();
        }
    }

    static class Repository{
        public void call() throws MyUncheckedException{
            throw new MyUncheckedException("ex");
        }
    }
}

