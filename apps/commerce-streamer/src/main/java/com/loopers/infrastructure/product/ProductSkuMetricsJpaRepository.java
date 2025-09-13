package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductSkuMetrics;
import com.loopers.domain.product.ProductSkuMetricsRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface ProductSkuMetricsJpaRepository extends JpaRepository<ProductSkuMetrics, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(value = """
        INSERT INTO product_sku_metrics (product_sku_id, product_id, sales_cnt, updated_at)
        VALUES (:skuId, :productId, :delta, :now)
        ON DUPLICATE KEY UPDATE
            sales_cnt = COALESCE(sales_cnt, 0) + :delta,
            updated_at = :now
        """, nativeQuery = true)
    void upsertAddSales(@Param("productId") Long productId,
                        @Param("skuId") Long skuId,
                        @Param("delta") Long delta,
                        @Param("now") Instant now);

    @Query("""
        select p.productId as productId,
               coalesce(sum(p.salesCnt), 0) as total
        from ProductSkuMetrics p
        where p.productId in :ids
        group by p.productId
        """)
    List<SalesSum> sumSalesByProductIds(@Param("ids") Collection<Long> ids);

}
