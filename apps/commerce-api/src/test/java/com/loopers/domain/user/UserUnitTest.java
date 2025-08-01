package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UserUnitTest {

    @DisplayName("유저를 생성할 때, ")
    @Nested
    class SignUp{

        @DisplayName("ID 가 `영문 및 숫자 10자 이내` 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        @ParameterizedTest
        @ValueSource(strings = {
                "login_id",
                "",
                "userIduserId",
                "userId12345",
                "12345678987654321"
        })
        void failure_createUser_whenLoginIdIsInvalid(String userId) {

            CoreException exception = assertThrows(CoreException.class, () -> {
                new User(
                        userId,
                        User.Gender.M,
                        "사용자1",
                        "2025-07-07",
                        "xx@yy.zz"
                );
            });

            assertAll(
                    () -> assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST),
                    () -> assertThat(exception.getMessage()).isEqualTo("ID는 영문 및 숫자 10자 이내여야 합니다.")
            );
        }

        @DisplayName("이메일이 `xx@yy.zz` 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        @ParameterizedTest
        @ValueSource(strings = {
                "user@emmail.com.com",
                "user@user@emmail.com.com",
                "",
                "emailemailemail",
                "user@emmail@com",
                "user.emmail.com"
        })
        void failure_createUser_whenEmailIsInvalid(String email) {

            CoreException exception = assertThrows(CoreException.class, () -> {
                new User(
                        "loginId1",
                        User.Gender.M,
                        "사용자1",
                        "2025-07-07",
                        email
                );
            });

            assertAll(
                    () -> assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST),
                    () -> assertThat(exception.getMessage()).isEqualTo("이메일 형식이 올바르지 않습니다.")
            );
        }

        @DisplayName("생년월일이 `yyyy-MM-dd` 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        @ParameterizedTest
        @ValueSource(strings = {
                "07-07-2025",
                "",
                "07-07-25",
                "07.07.2025"
        })
        void failure_createUser_whenBirthDateIsInvalid(String birth) {

            CoreException exception = assertThrows(CoreException.class, () -> {
                new User(
                        "loginId1",
                        User.Gender.M,
                        "사용자1",
                        birth,
                        "xx@yy.zz"
                );
            });

            assertAll(
                    () -> assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST),
                    () -> assertThat(exception.getMessage()).isEqualTo("생년월일은 yyyy-MM-dd 형식이어야 합니다.")
            );
        }

        @DisplayName("성별이 null이면 User 객체 생성에 실패한다.")
        @Test
        void failure_createUser_whenGenderIsNull() {

            CoreException exception =  assertThrows(CoreException.class, () -> {
                new User(
                        "loginId1",
                        null,
                        "사용자1",
                        "2025-07-07",
                        "xx@yy.zz"
                );
            });

            assertAll( // 이렇게 감싸는거랑 아닌거랑 뭔차이지?
                    () -> assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST),
                    () -> assertThat(exception.getMessage()).isEqualTo("성별은 필수값 입니다.")
            );

        }
    }
}
