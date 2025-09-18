package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductMetrics;
import com.loopers.domain.product.ProductMetricsId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductMetricsJpaRepository extends JpaRepository<ProductMetrics, ProductMetricsId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select pm from ProductMetrics pm
        where pm.id.productId = :productId and pm.id.date = :date
    """)
    Optional<ProductMetrics> findByPkForUpdate(@Param("productId") Long productId,
                                               @Param("date") LocalDate date);

    @Query("""
        select pm from ProductMetrics pm
        where pm.id.productId in :productIds and pm.id.date = :date
    """)
    List<ProductMetrics> findAllByIdProductIdInAndIdDate(@Param("productIds") Set<Long> productIds,
                                                         @Param("date") LocalDate date);

    @Query("""
        select coalesce(sum(pm.viewCntDelta), 0)
        from ProductMetrics pm
        where pm.id.productId = :productId and pm.id.date between :from and :to
    """)
    long sumViewDeltaBetween(@Param("productId") Long productId,
                             @Param("from") LocalDate from,
                             @Param("to") LocalDate to);

    @Query("""
        select coalesce(sum(pm.likeCntDelta), 0)
        from ProductMetrics pm
        where pm.id.productId = :productId and pm.id.date between :from and :to
    """)
    long sumLikeDeltaBetween(@Param("productId") Long productId,
                             @Param("from") LocalDate from,
                             @Param("to") LocalDate to);
}
