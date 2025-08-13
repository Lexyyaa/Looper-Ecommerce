package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DisplayName("ProductService 통합 테스트")
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown(){
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("[상품 단건 조회]")
    class GetProduct {

        @Test
        @DisplayName("[성공] 활성 상태의 상품 조회 성공")
        void success_getProduct() {
            Product product = productRepository.saveProduct(Product.create("맥북에어", Product.Status.ACTIVE,1L));
            Product result = productService.getProduct(product.getId());

            assertEquals(product.getName(), result.getName());
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 상품 조회 시 예외 발생한다.")
        void failure_getProduct_whenNotFound() {
            CoreException exception = assertThrows(CoreException.class, () -> productService.getProduct(999L));
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        @Test
        @DisplayName("[실패] 일시품절/품절 상품 조회 시 예외 발생한다.")
        void failure_getProduct_whenSoldOut() {
            Product currProduct = Product.create("맥북프로", Product.Status.ACTIVE,1L);
            currProduct.changeStatus(Product.Status.SOLD_OUT);
            Product soldOut = productRepository.saveProduct(currProduct);

            CoreException exception = assertThrows(CoreException.class, () -> productService.getProduct(soldOut.getId()));
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("[상품 상태 업데이트]")
    class UpdateStatus {

        @Test
        @DisplayName("[성공] 상품이 모두 품절 상태일 경우 상태를 SOLD_OUT으로 변경한다.")
        void success_updateStatusToSoldOut() {
            Product product = productRepository.saveProduct(Product.create("맥북에어", Product.Status.ACTIVE,1L));

            productService.updateStatus(true, product.getId());

            Product updated = productRepository.findBy(product.getId()).orElseThrow();
            assertEquals(Product.Status.SOLD_OUT, updated.getStatus());
        }

        @Test
        @DisplayName("[성공] 재고가 남아있을 경우 상태 변경은 하지 않는다.")
        void success_doNotUpdate_whenStockExists() {
            Product product = productRepository.saveProduct(Product.create("맥북에어", Product.Status.ACTIVE,1L));

            productService.updateStatus(false, product.getId());

            Product updated = productRepository.findBy(product.getId()).orElseThrow();
            assertEquals(Product.Status.ACTIVE, updated.getStatus());
        }
    }
}
