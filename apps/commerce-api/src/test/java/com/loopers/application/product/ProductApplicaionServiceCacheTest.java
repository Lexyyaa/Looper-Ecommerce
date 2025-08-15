package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.*;
import com.loopers.testcontainers.utils.RedisCleanUp;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ProductApplicaionServiceCacheTest {
    @Autowired
    ProductApplicationService productApplicationService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @MockBean
    ProductService productService;
    @MockBean
    ProductSkuService productSkuService;
    @MockBean
    BrandService brandService;

    private static final String CACHE_KEY_PREFIX = "product:detail::";
    private Long productId;
    private Long brandId;
    private Product product;
    private Brand brand;
    private List<ProductSku> skus;

    @BeforeEach
    void setUp() {
        productId = 1L;
        brandId = 10L;
        brand = Brand.create("브랜드A", "브랜드A");

        product = Product.create("맥북프로", Product.Status.ACTIVE, brandId);
        product.updateLikeCnt(10L);
        product.updateMinPrice(2000L);

        ProductSku sku1 = ProductSku.create(product, "macbookpro-silver-16", 2000, 10, 0);
        ProductSku sku2 = ProductSku.create(product, "macbookpro-silver-14", 2200, 15, 0);
        skus = List.of(sku1, sku2);

        when(productService.getProduct(productId)).thenReturn(product);
        when(productSkuService.getByProductId(productId)).thenReturn(skus);
        when(brandService.get(brandId)).thenReturn(brand);
    }

    @AfterEach
    public void cleanUp() {
        redisCleanUp.truncateAll();
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("[상품상세조회 - 캐시읽기]")
    class ReadCacheTests {

        @Test
        @DisplayName("[성공] 첫 조회는 DB를 호출하고 캐시에 저장한다")
        void success_getProductDetail_whenThereAreNotDataCache() {
            // Act
            ProductInfo.Detail detail = productApplicationService.getProductDetailWithCacheable(productId);

            // Assert
            Mockito.verify(productService, times(1)).getProduct(productId);
            Mockito.verify(productSkuService, times(1)).getByProductId(productId);
            Mockito.verify(brandService, times(1)).get(brandId);

            Object cachedValue = redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + productId);

            assertThat(cachedValue).isNotNull();
            assertThat(detail.name()).isEqualTo("맥북프로");
        }

        @Test
        @DisplayName("[성공] 캐시가 있을 경우, DB를 호출하지 않는다")
        void success_getProductDetail_UsingCache() {
            productApplicationService.getProductDetailWithCacheable(productId);
            Mockito.clearInvocations(productService, productSkuService, brandService);

            productApplicationService.getProductDetailWithCacheable(productId);

            Mockito.verify(productService, times(0)).getProduct(any());
            Mockito.verify(productSkuService, times(0)).getByProductId(any());
            Mockito.verify(brandService, times(0)).get(any());
        }
    }

    @Nested
    @DisplayName("updateProductWithEvictCache - 캐시 삭제")
    class UpdateCacheTests {

        @Test
        @DisplayName("[성공] 상품 업데이트 시 @CacheEvict에 의해 캐시가 삭제된다")
        void success_evictCache_whenUpdateProduct() {
            productApplicationService.getProductDetailWithCacheable(productId);
            assertThat(redisTemplate.hasKey(CACHE_KEY_PREFIX + productId)).isTrue();

            ProductCommand.Update command = new ProductCommand.Update(
                    productId,
                    "업데이트된 맥북프로",
                    Product.Status.ACTIVE,
                    brand.getId(),
                    brand.getName(),
                    List.of(new ProductCommand.Sku(1L, "sku-update", 20000, 5,0))
            );

            when(productService.getProduct(productId)).thenReturn(product);
            when(productSkuService.saveProductSkus(any(), any())).thenReturn(skus);
            when(productSkuService.getMinPrice(productId)).thenReturn(2500L);
            when(productService.saveProduct(any(), any())).thenReturn(product);

            productApplicationService.updateProductWithEvictCache(command);

            assertThat(redisTemplate.hasKey(CACHE_KEY_PREFIX + productId)).isFalse();
        }
    }
}
