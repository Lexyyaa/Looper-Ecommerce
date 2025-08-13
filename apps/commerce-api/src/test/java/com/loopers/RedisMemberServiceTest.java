package com.loopers;

import com.loopers.config.redis.RedisConfig;
import com.loopers.redisexample.Member;
import com.loopers.redisexample.RedisMemberService;
import com.loopers.testcontainers.RedisTestContainersConfig;
import com.loopers.testcontainers.utils.RedisCleanUp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
@SpringBootTest
//@ContextConfiguration(classes = {RedisTestContainersConfig.class, RedisConfig.class})
class RedisMemberServiceTest {

    @Autowired
    private RedisMemberService redisMemberService;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @BeforeEach
    void setUp() {
        redisCleanUp.truncateAll();
        log.info(">>>> 테스트 후 Redis 데이터 정리 완료.");
    }

    @DisplayName("단일 멤버 객체를 Redis에 저장하고 조회한다.")
    @Test
    void testSaveAndFindMember() {
        log.info("==== 단일 멤버 테스트 시작 ====");

        // given
        String key = "member:1";
        Member member = new Member("1", "Alice", 30);
        Duration ttl = Duration.ofMinutes(1);
        log.info(">>>> 저장할 멤버: {}, 키: {}", member, key);

        // when
        redisMemberService.saveMember(key, member, ttl);
        log.info(">>>> 멤버 저장 완료. 조회 시작...");
        Member foundMember = redisMemberService.findMember(key);
        log.info(">>>> 조회된 멤버: {}", foundMember);

        // then
        assertThat(foundMember).isEqualTo(member);
        log.info(">>>> 검증 성공: 저장된 멤버와 조회된 멤버가 동일함.");
    }

    @DisplayName("존재하지 않는 키로 멤버를 조회하면 null을 반환한다.")
    @Test
    void testFindMemberWithNonExistingKey() {
        log.info("==== 존재하지 않는 키 조회 테스트 시작 ====");

        // given
        String nonExistingKey = "member:non-existing";
        log.info(">>>> 존재하지 않는 키: {}", nonExistingKey);

        // when
        Member foundMember = redisMemberService.findMember(nonExistingKey);
        log.info(">>>> 조회된 결과: {}", foundMember);

        // then
        assertNull(foundMember);
        log.info(">>>> 검증 성공: 결과가 null임.");
    }

    @DisplayName("멤버 리스트를 Redis에 저장하고 조회한다.")
    @Test
    void testSaveAndFindMembersList() {
        log.info("==== 멤버 리스트 테스트 시작 ====");

        // given
        String key = "members:list";
        List<Member> members = List.of(
                new Member("2", "Bob", 25),
                new Member("3", "Charlie", 35)
        );
        Duration ttl = Duration.ofMinutes(5);
        log.info(">>>> 저장할 멤버 리스트: {}, 키: {}", members, key);

        // when
        redisMemberService.saveMembers(key, members, ttl);
        log.info(">>>> 멤버 리스트 저장 완료. 조회 시작...");
        List<Member> foundMembers = redisMemberService.findMembers(key);
        log.info(">>>> 조회된 멤버 리스트: {}", foundMembers);

        // then
        assertThat(foundMembers).containsExactlyElementsOf(members);
        log.info(">>>> 검증 성공: 저장된 리스트와 조회된 리스트가 동일함.");
    }

    @DisplayName("존재하지 않는 키로 멤버 리스트를 조회하면 null을 반환한다.")
    @Test
    void testFindMembersListWithNonExistingKey() {
        log.info("==== 존재하지 않는 키 리스트 조회 테스트 시작 ====");

        // given
        String nonExistingKey = "members:list:non-existing";
        log.info(">>>> 존재하지 않는 키: {}", nonExistingKey);

        // when
        List<Member> foundMembers = redisMemberService.findMembers(nonExistingKey);
        log.info(">>>> 조회된 결과: {}", foundMembers);

        // then
        assertNull(foundMembers);
        log.info(">>>> 검증 성공: 결과가 null임.");
    }
}
