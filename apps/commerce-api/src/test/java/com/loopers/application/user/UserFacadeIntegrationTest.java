package com.loopers.application.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class UserFacadeIntegrationTest {

    @Autowired
    private UserFacade userFacade;

    @DisplayName("[내정보조회]  ")
    @Nested
    class MyProfile {
        @DisplayName("[내정보조회 실패] 해당 ID의 회원이 존재하지 않을 경우, `400 Not Found` 을 반환한다.")
        @Test
        void failure_myProfile_whenUserDoesNotExist() {

            // arrange
            String loginId = "loginId111";

            // act
            CoreException exception =  assertThrows(CoreException.class,
                    () ->  userFacade.myProfile(loginId)
            );

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
