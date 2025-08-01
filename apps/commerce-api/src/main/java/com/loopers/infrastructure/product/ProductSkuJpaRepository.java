package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductSkuJpaRepository extends JpaRepository<ProductSku, Long> {

    @Query("SELECT s FROM ProductSku s WHERE s.product.id = :productId")
    List<ProductSku> findAllByProductId(@Param("productId") Long productId);

    @Query("""
    SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
    FROM ProductSku s
    WHERE s.product.id = :productId
      AND (s.stockTotal - s.stockReserved) > 0
""")
    boolean existsAvailableStock(@Param("productId") Long productId);

}



