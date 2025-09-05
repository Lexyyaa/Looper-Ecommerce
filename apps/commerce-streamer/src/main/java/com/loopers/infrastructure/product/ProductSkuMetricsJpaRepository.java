package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductSkuMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

public interface ProductSkuMetricsJpaRepository extends JpaRepository<ProductSkuMetrics, Long> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO product_sku_metrics (product_sku_id, product_id, sales_cnt, updated_at)
        VALUES (:skuId, :productId, :delta, :now)
        ON DUPLICATE KEY UPDATE
            sales_cnt = sales_cnt + VALUES(sales_cnt),
            updated_at = VALUES(updated_at)
        """, nativeQuery = true)
    void upsertAddSales(@Param("productId") Long productId,
                        @Param("skuId") Long skuId,
                        @Param("delta") Long delta,
                        @Param("now") Instant now);
}
