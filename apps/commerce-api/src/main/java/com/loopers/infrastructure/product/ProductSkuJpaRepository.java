package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductSku;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductSkuJpaRepository extends JpaRepository<ProductSku, Long> {

    @Query("SELECT s FROM ProductSku s WHERE s.product.id = :productId")
    List<ProductSku> findAllByProductId(@Param("productId") Long productId);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT ps FROM ProductSku ps WHERE ps.id = :id")
    Optional<ProductSku> findByIdWithOptimisticLock(@Param("id") Long id);

    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM ProductSku s
        WHERE s.product.id = :productId
          AND (s.stockTotal - s.stockReserved) > 0
    """)
    boolean existsAvailableStock(@Param("productId") Long productId);

    @Query("SELECT MIN(ps.price) FROM ProductSku ps WHERE ps.product.id = :productId")
    Long findMinPriceByProductId(@Param("productId") Long productId);
}



