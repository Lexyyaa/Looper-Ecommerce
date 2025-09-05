package com.loopers.infrastructure.persistance.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeTargetType;
import com.loopers.domain.like.LikedProductProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeJpaRepository extends JpaRepository<Like, Long> {

    @Query("""
        SELECT COUNT(l)
        FROM Like l
        WHERE l.targetId = :targetId
          AND l.targetType = :targetType
    """)
    long countByTargetIdAndTargetType(
            @Param("targetId") Long targetId,
            @Param("targetType") LikeTargetType targetType
    );

    @Query("""
        SELECT l FROM Like l 
        WHERE l.userId = :userId 
          AND l.targetId = :targetId 
          AND l.targetType = :targetType
    """)
    Optional<Like> findLike(
            Long userId, Long targetId,
            LikeTargetType targetType
    );

    @Query("""
        SELECT COUNT(l) > 0 FROM Like l 
        WHERE l.userId = :userId 
          AND l.targetId = :targetId 
          AND l.targetType = :targetType
    """)
    boolean existsLike(
            Long userId,
            Long targetId,
            LikeTargetType targetType
    );

    @Query(value = """
        SELECT
            p.id          AS id,
            p.name        AS name,
            COALESCE(MIN(s.price), 0)           AS minPrice,
            COUNT(DISTINCT l2.id)               AS likeCount, 
            p.status      AS status,
            p.created_at  AS createdAt
        FROM `likes` l                                        
        JOIN product p ON p.id = l.target_id
        LEFT JOIN product_sku s ON s.product_id = p.id
        LEFT JOIN `likes` l2 ON l2.target_id = p.id AND l2.target_type = 'PRODUCT'
        WHERE l.user_id = :userId
          AND l.target_type = 'PRODUCT'
        GROUP BY p.id, p.name, p.status, p.created_at
        ORDER BY MAX(l.created_at) DESC  
        LIMIT :limit OFFSET :offset;
    """, nativeQuery = true)
    List<LikedProductProjection> findLikedProducts(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );
}
