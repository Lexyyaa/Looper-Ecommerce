package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DisplayName("ProductSkuService 통합 테스트")
public class ProductSkuServiceIntegrationTest {

    @Autowired
    ProductRepository productRepository;
    @Autowired
    ProductSkuService productSkuService;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    Long productId;
    Long skuId;

    @BeforeEach
    void setUp() {
        Product product = Product.create("맥북프로", Product.Status.ACTIVE,1L);
        product = productRepository.saveProduct(product);
        productId = product.getId();

        ProductSku sku = ProductSku.create(product, "macbookpro-gray-16",1000, 10, 0);
        sku = productRepository.saveProductSkuAndFlush(sku);
        skuId = sku.getId();
    }

    @AfterEach
    void tearDown(){
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("[성공] SKU를 단건으로 조회한다")
    void success_getBySkuId() {
        ProductSku found = productSkuService.getBySkuId(skuId);
        assertThat(found.getId()).isEqualTo(skuId);
        assertThat(found.availableQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 SKU를 조회하면 NOT_FOUND 예외가 발생한다")
    void failure_getBySkuId_whenNotFound() {
        CoreException ex = assertThrows(CoreException.class,
                () -> productSkuService.getBySkuId(999999L));
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
    }

    @Test
    @DisplayName("[실패] 가용 수량을 초과하여 재고를 선점하면 BAD_REQUEST 예외가 발생한다")
    void failure_reserveStock_whenExceed() {
        ProductSku sku = productSkuService.getBySkuId(skuId);
        CoreException ex = assertThrows(CoreException.class, () -> productSkuService.reserveStock(sku.getId(), 11));
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @Test
    @DisplayName("[실패] 재고 선점 수량이 0인 경우 BAD_REQUEST 예외가 발생한다")
    void failure_reserveStock_zeroQuantity() {
        ProductSku sku = productSkuService.getBySkuId(skuId);
        CoreException ex = assertThrows(CoreException.class, () -> productSkuService.reserveStock(sku.getId(), 0));
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }
}
