package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductSkuMetrics;
import com.loopers.domain.product.ProductSkuMetricsId;
import com.loopers.domain.product.ProductSkuMetricsRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductSkuMetricsJpaRepository extends JpaRepository<ProductSkuMetrics, ProductSkuMetricsId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select psm from ProductSkuMetrics psm
        where psm.id.productSkuId = :skuId and psm.id.date = :date
    """)
    Optional<ProductSkuMetrics> findByPkForUpdate(@Param("skuId") Long productSkuId,
                                                  @Param("date") LocalDate date);

    @Query("""
    select psm.productId as productId,
           coalesce(sum(psm.salesCntDelta), 0) as total
    from ProductSkuMetrics psm
    where psm.productId in :productIds
      and psm.id.date = :date
    group by psm.productId
    """)
    List<SalesSum> sumSalesByProductIdsAndDate(@Param("productIds") Set<Long> productIds,
                                               @Param("date") LocalDate date);

    @Query("""
    select psm.productId as productId,
           coalesce(sum(psm.salesCntDelta), 0) as total
    from ProductSkuMetrics psm
    where psm.productId in :productIds
      and psm.id.date between :start and :end
    group by psm.productId
    """)
    List<SalesSum> sumSalesByProductIdsBetween(@Param("productIds") Set<Long> productIds,
                                               @Param("start") LocalDate start,
                                               @Param("end") LocalDate end);

    public interface SalesSum {
        Long getProductId();
        Long getTotal();
    }
}
