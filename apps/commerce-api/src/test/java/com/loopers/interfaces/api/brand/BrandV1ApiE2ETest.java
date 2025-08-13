package com.loopers.interfaces.api.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.controller.brand.BrandV1Response;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BrandV1ApiE2ETest {

    @Autowired
    private TestRestTemplate rest;
    @Autowired private BrandJpaRepository brandJpaRepository;
    @Autowired private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() { databaseCleanUp.truncateAllTables(); }

    @Test
    @DisplayName("[브랜드 단건 조회] 존재하는 브랜드를 조회하면 200과 응답을 반환한다.")
    void success_getBrand() {
        Brand brand = brandJpaRepository.save(Brand.create("브랜드X", "설명"));

        var type = new ParameterizedTypeReference<ApiResponse<BrandV1Response>>() {};
        ResponseEntity<ApiResponse<BrandV1Response>> res =
                rest.exchange("/api/v1/brands/{id}", HttpMethod.GET, null, type, brand.getId());

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().data().name()).isEqualTo("브랜드X");
    }

    @Test
    @DisplayName("[브랜드 단건 조회] 존재하지 않는 브랜드는 404를 반환한다.")
    void failure_getBrand_notFound() {
        var type = new ParameterizedTypeReference<ApiResponse<BrandV1Response>>() {};
        ResponseEntity<ApiResponse<BrandV1Response>> res =
                rest.exchange("/api/v1/brands/{id}", HttpMethod.GET, null, type, 999999L);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
