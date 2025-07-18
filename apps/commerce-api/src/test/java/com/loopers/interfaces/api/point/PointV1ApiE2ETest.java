package com.loopers.interfaces.api.point;

import com.loopers.domain.user.UserEntity;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.controller.point.PointV1Request;
import com.loopers.interfaces.api.controller.point.PointV1Response;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PointV1ApiE2ETest {

    private final TestRestTemplate testRestTemplate;
    private final UserJpaRepository userJpaRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public PointV1ApiE2ETest(
            TestRestTemplate testRestTemplate,
            UserJpaRepository userJpaRepository,
            DatabaseCleanUp databaseCleanUp
    ) {
        this.testRestTemplate = testRestTemplate;
        this.userJpaRepository = userJpaRepository;
        this.databaseCleanUp = databaseCleanUp;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("[포인트 충전] /api/v1/points ")
    @Nested
    class Charge {

        private final String END_POINT = "/api/v1/points/charge";

        @DisplayName("존재하는 유저가 1000원을 충전하면 보유 총량을 응답으로 반환한다.")
        @Test
        void success_charge_whenUserExistsAndAmountIsValid() {

            String loginId = "user111";
            UserEntity user = new UserEntity(
                    loginId,
                    UserEntity.Gender.M,
                    "사용자1",
                    "2025-07-07",
                    "loginId123@user.com",
                    1000L
            );
            userJpaRepository.save(user);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", loginId);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Long amount = 1000L;
            PointV1Request.Charge request = new PointV1Request.Charge(amount);

            HttpEntity<PointV1Request.Charge> entity = new HttpEntity<>(request, headers);

            // act
            ParameterizedTypeReference<ApiResponse<PointV1Response>> responseType =
                    new ParameterizedTypeReference<>() {
                    };
            ResponseEntity<ApiResponse<PointV1Response>> response =
                    testRestTemplate.exchange(
                            END_POINT,
                            HttpMethod.POST,
                            entity,
                            responseType
                    );

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().loginId()).isEqualTo(loginId),
                    () -> assertThat(response.getBody().data().name()).isEqualTo(user.getName()),
                    () -> assertThat(response.getBody().data().point()).isEqualTo(user.getPoint() + amount)
            );
        }

        @DisplayName("존재하지 않는 유저가 충전하면 404 Not Found를 반환한다.")
        @Test
        void failure_charge_whenUserDoesNotExist() {

            // arrange
            String loginId = "user222";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", loginId);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Long amount = 1000L;
            PointV1Request.Charge request = new PointV1Request.Charge(amount);

            HttpEntity<PointV1Request.Charge> entity = new HttpEntity<>(request, headers);

            // act
            ParameterizedTypeReference<ApiResponse<PointV1Response>> responseType =
                    new ParameterizedTypeReference<>() {
                    };
            ResponseEntity<ApiResponse<PointV1Response>> response =
                    testRestTemplate.exchange(
                            END_POINT,
                            HttpMethod.POST,
                            entity,
                            responseType
                    );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        }
    }
}

