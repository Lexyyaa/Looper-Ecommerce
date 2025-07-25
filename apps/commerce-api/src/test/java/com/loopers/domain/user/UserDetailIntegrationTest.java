package com.loopers.domain.user;

import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.infrastructure.user.UserRepositoryImpl;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserDetailIntegrationTest {

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

    @DisplayName("[회원가입]  ")
    @Nested
    class SignUp {

        @DisplayName("[회원가입 성공] 로그인 ID 가 중복되지 않으면 회원가입이 성공하고 DB 에 저장된다.")
        @Test
        void success_signUp_whenLoginIdIsUnique() {
            // arrange
            UserCommand.SignUp command = new UserCommand.SignUp(
                    "loginId123",
                    UserEntity.Gender.M,
                    "사용자1",
                    "2025-07-07",
                    "loginId123@user.com"
            );

            Mockito.doReturn(false)
                    .when(userRepository).existsByLoginId(command.loginId());
            // act
            UserInfo.UserDetail info = userService.signUp(command);

            // assert
            assertThat(info).isNotNull();
            assertThat(info.loginId()).isEqualTo("loginId123");
            assertThat(info.name()).isEqualTo("사용자1");

            verify(userRepository).save(any(UserEntity.class));
        }

        @DisplayName("[회원가입 실패]이미 존재하는 로그인 ID 로 회원가입을 시도하면 BAD_REQUEST 가 발생하고 저장은 실행되지 않는다.")
        @Test
        void failure_signUp_whenLoginIdExists() {

            //arrange
            UserEntity saved = new UserEntity(
                    "loginId123",
                    UserEntity.Gender.M,
                    "사용자1",
                    "2025-07-07",
                    "loginId123@user.com"
            );
            userRepository.save(saved);

            UserCommand.SignUp command = new UserCommand.SignUp(
                    "loginId123", UserEntity.Gender.M,"사용자2","2025-07-07","loginId123@user.com"
            );

            // act
            CoreException exception =  assertThrows(CoreException.class,
                    () -> userService.signUp(command)
            );

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("[내정보조회]  ")
    @Nested
    class MyProfile {

        @DisplayName("[내정보조회 성공] 회원이 존재하면 프로필을 반환한다.")
        @Test
        void success_myProfile_whenUserExists() {

            // arrange
            UserEntity saved = new UserEntity(
                    "loginId123",
                    UserEntity.Gender.M,
                    "사용자1",
                    "2025-07-07",
                    "loginId123@user.com"
            );
            userRepository.save(saved);

            // act
            UserInfo.UserDetail info = userService.myProfile(saved.getLoginId());

            // assert
            assertThat(info).isNotNull();
            assertThat(info.loginId()).isEqualTo("loginId123");
            assertThat(info.name()).isEqualTo("사용자1");

        }

        @DisplayName("[내정보조회 실패] 회원이 없을 경우 null 반환한다.")
        @Test
        void failure_myProfile_whenUserDoesNotExist() {

            // arrange
            String loginId = "loginId111";

            // act
            UserInfo.UserDetail info = userService.myProfile(loginId);

            // assert
            assertThat(info).isNull();
        }
    }
}
