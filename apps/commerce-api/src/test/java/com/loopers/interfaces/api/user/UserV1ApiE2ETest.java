package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserEntity;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.controller.user.UserV1Request;
import com.loopers.interfaces.api.controller.user.UserV1Response;
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
public class UserV1ApiE2ETest {

    private final TestRestTemplate testRestTemplate;
    private final UserJpaRepository userJpaRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public UserV1ApiE2ETest(
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

    @DisplayName("[회원가입] Post /api/v1/users ")
    @Nested
    class SignUp {

        private final String END_POINT = "/api/v1/users";

        @DisplayName("회원 가입에 성공하면 생성된 유저 정보를 응답으로 반환한다.")
        @Test
        void success_signUp_whenRequestIsValid() {

            //arrange
            UserV1Request.SignUp request = new UserV1Request.SignUp(
                    "user123",
                    UserEntity.Gender.M,
                    "사용자123",
                    "2025-07-07",
                    "user123@email.com"
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<UserV1Request.SignUp> entity = new HttpEntity<>(request, headers);

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Response>> responseType = new ParameterizedTypeReference<ApiResponse<UserV1Response>>() {
            };
            ResponseEntity<ApiResponse<UserV1Response>> response =
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
                    () -> assertThat(response.getBody().data().loginId()).isEqualTo(request.loginId()),
                    () -> assertThat(response.getBody().data().name()).isEqualTo(request.name()),
                    () -> assertThat(response.getBody().data().birth()).isEqualTo(request.birth())
            );
        }

        @DisplayName("회원 가입 시 성별이 없으면 400 Bad Request를 반환한다.")
        @Test
        void failure_signUp_whenGenderIsMissing() {
            //arrange
            UserV1Request.SignUp request = new UserV1Request.SignUp(
                    "user123",
                    null,
                    "사용자123",
                    "2025-07-07",
                    "user123@email.com"
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<UserV1Request.SignUp> entity = new HttpEntity<>(request, headers);

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Response>> responseType = new ParameterizedTypeReference<ApiResponse<UserV1Response>>() {
            };
            ResponseEntity<ApiResponse<UserV1Response>> response =
                    testRestTemplate.exchange(
                            END_POINT,
                            HttpMethod.POST,
                            entity,
                            responseType
                    );

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @DisplayName("[내정보조회] GET /api/v1/users/me ")
    @Nested
    class MyProfile {

        private final String END_POINT = "/api/v1/users/me";

        @DisplayName("내 정보 조회에 성공하면 유저 정보를 응답으로 반환한다.")
        @Test
        void success_myProfile_whenUserExists() {
            // arrange
            String loginId = "user123";
            UserEntity user = new UserEntity(
                    loginId,
                    UserEntity.Gender.M,
                    "사용자1",
                    "2025-07-07",
                    "loginId123@user.com"
            );
            userJpaRepository.save(user);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", loginId);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Response>> responseType =
                    new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Response>> response =
                    testRestTemplate.exchange(
                            END_POINT,
                            HttpMethod.GET,
                            entity,
                            responseType
                    );

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().loginId()).isEqualTo(loginId),
                    () -> assertThat(response.getBody().data().name()).isEqualTo(user.getName()),
                    () -> assertThat(response.getBody().data().birth()).isEqualTo(user.getBirth())
            );
        }

        @DisplayName("존재하지 않는 ID로 조회하면 404 Not Found를 반환한다.")
        @Test
        void failure_myProfile_whenUserDoesNotExist() {
            // arrange
            String loginId = "user123";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", loginId);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Response>> responseType =
                    new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Response>> response =
                    testRestTemplate.exchange(
                            END_POINT,
                            HttpMethod.GET,
                            entity,
                            responseType
                    );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}

