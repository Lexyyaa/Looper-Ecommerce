package com.loopers.domain.like;

import java.util.List;
import java.util.Optional;

public interface LikeRepository {
    void save(Like like);

    void delete(Like like);

    Optional<Like> findLike(Long userId, Long targetId, LikeTargetType targetType);

    long countByTargetId(Long targetId, LikeTargetType targetType);

    List<LikedProductProjection> findLikedProducts(Long userId, int offset, int limit);
}
