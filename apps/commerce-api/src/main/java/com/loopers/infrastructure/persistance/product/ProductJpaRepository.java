package com.loopers.infrastructure.persistance.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    @Query(value = """
        SELECT
            p.id AS id,
            p.name AS name,
            p.min_price as minPrice,
            p.like_Cnt AS likeCount,
            p.status AS status,
            p.created_at AS createdAt
        FROM product p
        WHERE p.status = 'ACTIVE'
        AND (:brandId IS NULL OR p.brand_id = :brandId)
        ORDER BY
            CASE :sort
                WHEN 'RECENT' THEN p.created_at
                ELSE NULL
            END DESC,
            CASE :sort
                WHEN 'LOW_PRICE' THEN minPrice
                ELSE NULL
            END ASC,
            CASE :sort
                WHEN 'LIKE' THEN likeCount
                ELSE NULL
            END DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<ProductSummaryProjection> findProductSummary(
            @Param("brandId") Long brandId,
            @Param("sort") String sort,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(value = """
        SELECT
            p.id AS id,
            p.name AS name,
            (
                SELECT MIN(s.price)
                FROM product_sku s
                WHERE s.product_id = p.id
            ) AS minPrice,
            (
                SELECT COUNT(l.id)
                FROM likes l
                WHERE l.target_id = p.id AND l.target_type = 'PRODUCT'
            ) AS likeCount,
            p.status AS status,
            p.created_at AS createdAt
        FROM product p
        WHERE p.status = 'ACTIVE'
        AND (:brandId IS NULL OR p.brand_id = :brandId)
        ORDER BY
            CASE :sort
                WHEN 'RECENT' THEN p.created_at
                ELSE NULL
            END DESC,
            CASE :sort
                WHEN 'LOW_PRICE' THEN minPrice
                ELSE NULL
            END ASC,
            CASE :sort
                WHEN 'LIKE' THEN likeCount
                ELSE NULL
            END DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<ProductSummaryProjection> findProductSummaries(
            @Param("brandId") Long brandId,
            @Param("sort") String sort,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
}
