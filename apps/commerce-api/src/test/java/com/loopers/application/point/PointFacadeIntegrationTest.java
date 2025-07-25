package com.loopers.application.point;

import com.loopers.application.user.UserFacade;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class PointFacadeIntegrationTest {

    @Autowired
    private PointFacade pointFacade;

    @DisplayName("[포인트조회]  ")
    @Nested
    class MyProfile {
        @DisplayName("[포인트조회 실패] 해당 ID의 회원이 존재하지 않을 경우, `400 Not Found` 을 반환한다.")
        @Test
        void failure_myPoint_whenUserDoesNotExist() {

            // arrange
            String loginId = "loginId111";

            // act
            CoreException exception =  assertThrows(CoreException.class,
                    () ->  pointFacade.myPoint(loginId)
            );

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
