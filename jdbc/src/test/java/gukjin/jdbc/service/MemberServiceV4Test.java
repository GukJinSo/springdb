package gukjin.jdbc.service;

import gukjin.jdbc.domain.Member;
import gukjin.jdbc.repository.MemberRepository;
import gukjin.jdbc.repository.MemberRepositoryV3;
import gukjin.jdbc.repository.MemberRepositoryV4_1;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 예외 누수 문제 해결
 * SQLException 제거
 *
 * MemberRepository 인터페이스에 의존
 */
@Slf4j
@SpringBootTest
class MemberServiceV4Test {
    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";
    @Autowired private MemberRepository memberRepository;
    @Autowired private MemberServiceV4 memberService;

    @TestConfiguration
    static class testConfig{

        public testConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        private final DataSource dataSource;
        @Bean
        MemberRepository memberRepository(){
            return new MemberRepositoryV4_1(dataSource);
        }
        @Bean
        MemberServiceV4 memberService(){
            return new MemberServiceV4(memberRepository());
        }
    }

    @Test
    void aopCheck(){
        log.info("memberRepo = {}", memberRepository.getClass());
        Method[] repoMethods = memberRepository.getClass().getDeclaredMethods();
        for (Method method : repoMethods) {
            log.info("memberRepo method = {}", method);
        }

        log.info("memberService = {}", memberService.getClass());
        Method[] serviceMethods = memberService.getClass().getDeclaredMethods();
        for (Method method : serviceMethods) {
            log.info("memberService method = {}", method);
        }

        Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
        Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue();
    }

    @AfterEach
    void after() {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }
    @Test
    @DisplayName("정상 이체")
    void accountTransfer() {

        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);
        //when
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }
    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx() throws SQLException {

        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);
        //when
        assertThatThrownBy(() ->
                memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(),
                        2000))
                .isInstanceOf(IllegalStateException.class);
        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberEx =
                memberRepository.findById(memberEx.getMemberId());
        //memberA의 돈만 2000원 줄었고, ex의 돈은 10000원 그대로이다.
        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberEx.getMoney()).isEqualTo(10000);

    }
}