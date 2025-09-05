package com.loopers.infrastructure.persistance.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikeTargetType;
import com.loopers.domain.like.LikedProductProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LikeRepositoryImpl implements LikeRepository {

    private final LikeJpaRepository likeJpaRepository;

    @Override
    public void save(Like like) {
        likeJpaRepository.save(like);
    }

    @Override
    public void delete(Like like) {
        likeJpaRepository.delete(like);
    }

    @Override
    public Optional<Like> findLike(Long userId, Long targetId, LikeTargetType targetType) {
        return likeJpaRepository.findLike(userId, targetId, targetType);
    }

    @Override
    public long countByTargetId(Long targetId, LikeTargetType targetType) {
        return likeJpaRepository.countByTargetIdAndTargetType(targetId, targetType);
    }

    @Override
    public List<LikedProductProjection> findLikedProducts(Long userId, int offset, int limit) {
        return likeJpaRepository.findLikedProducts(userId, offset, limit);
    }

}
