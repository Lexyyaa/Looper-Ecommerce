package com.loopers.domain.point;

import com.loopers.domain.user.*;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.infrastructure.user.UserRepositoryImpl;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class PointIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown(){
        databaseCleanUp.truncateAllTables();
        Mockito.reset(userRepository);
    }

    @TestConfiguration
    static class SpyConfig {
        @Bean
        @Primary
        public UserRepository userRepositorySpy(UserJpaRepository jpaRepository) {
            return Mockito.spy(new UserRepositoryImpl(jpaRepository));
        }
    }

    @DisplayName("[포인트 충전] ")
    @Nested
    class Charge {

        @DisplayName("[포인트 충전 실패] 존재하지 않는 유저 ID로 충전을 시도하면 예외가 발생한다.")
        @Test
        void failure_charge_whenUserDoesNotExist() {
            // arrange
            UserEntity user = new UserEntity(
                    "loginId123",
                    UserEntity.Gender.M,
                    "사용자1",
                    "2025-07-07",
                    "loginId123@user.com"
            );
            userRepository.save(user);
            reset(userRepository);

            UserCommand.Charge command = new UserCommand.Charge("loginId111", 100L);

            Mockito.doReturn(false)
                    .when(userRepository).existsByLoginId(command.loginId());

            // act
            CoreException exception = assertThrows(CoreException.class,
                    () -> userService.charge(command)
            );

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);

            verify(userRepository).findByLoginId(command.loginId());
            verify(userRepository, never()).save(any());
            verifyNoMoreInteractions(userRepository);
        }
    }

    @DisplayName("[포인트 조회] ")
    @Nested
    class MyPoint {

        @DisplayName("[포인트 조회 성공] 회원이 존재하면 보유 포인트를 반환한다.")
        @Test
        void success_myPoint_whenUserExists() {
            // arrange
            UserEntity user = new UserEntity(
                    "loginId123",
                    UserEntity.Gender.M,
                    "사용자1",
                    "2025-07-07",
                    "loginId123@user.com",
                    1500L
            );
            userRepository.save(user);

            UserCommand.Charge command = new UserCommand.Charge("loginId123",500L);
            userService.charge(command);

            // act
            UserInfo.Point info = userService.myPoint(user.getLoginId());

            // assert
            assertThat(info).isNotNull();
            assertThat(info.loginId()).isEqualTo("loginId123");
            assertThat(info.name()).isEqualTo("사용자1");
            assertThat(info.point()).isEqualTo(user.getPoint()+command.amount());
        }

        @DisplayName("[포인트 조회 실패] 회원이 존재하지 않으면 null을 반환한다.")
        @Test
        void failure_myPoint_whenUserDoesNotExist() {
            // arrange
            String loginId = "loginId111";
            // act
            UserInfo.Point info = userService.myPoint(loginId);
            // assert
            assertThat(info).isNull();
        }
    }
}
