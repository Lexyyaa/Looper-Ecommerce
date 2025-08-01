package com.loopers.domain.like;

import com.loopers.domain.product.ProductSku;
import com.loopers.infrastructure.like.LikeJpaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeRepository {
    void save(Like like);

    void delete(Like like);

    Optional<Like> findLike(Long userId, Long targetId, LikeTargetType targetType);

    long countByTargetId(Long targetId, LikeTargetType targetType);

    List<LikedProductProjection> findLikedProducts(Long userId, int offset, int limit);
}
