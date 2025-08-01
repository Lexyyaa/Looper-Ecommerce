package com.loopers.domain.point;

import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PointUnitTest {

    @DisplayName("포인트를 충전할 때, ")
    @Nested
    class Charge {

        @DisplayName("0 이하의 정수로 포인트를 충전 시 400 에러가 발생한다.")
        @ParameterizedTest(name = "충전 금액: {0}")
        @ValueSource(longs = {0, -1000})
        void failure_charge_whenAmountIsZeroOrNegative(Long amount) {

            User user = new User(
                    "loginid",
                    User.Gender.M,
                    "사용자1",
                    "2025-07-07",
                    "xx@yy.zz"
            );

            CoreException exception = assertThrows(CoreException.class, () -> {
                user.charge(amount);
            });

            assertAll(
                    () -> assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST),
                    () -> assertThat(exception.getMessage()).isEqualTo("충전 금액은 0 보다 커야 합니다.")
            );
        }
    }
}
