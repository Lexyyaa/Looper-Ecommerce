package com.loopers.domain.like;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;

    public void save(Long userId, Long targetId, LikeTargetType targetType) {
        Like like = Like.create(userId, targetId, targetType);
        likeRepository.save(like);
    }

    public void delete(Long userId,  Long targetId, LikeTargetType targetType) {
        Like like = likeRepository.findLike(userId, targetId, targetType)
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST,"해당 좋아요가 존재하지 않습니다. "));
        likeRepository.delete(like);
    }

    public long getLikeCount(Long targetId, LikeTargetType targetType) {
        return likeRepository.countByTargetId(targetId, targetType);
    }

    public List<LikedProduct> getLikedProducts(Long userId, int page, int size) {
        return likeRepository.findLikedProducts(userId, page * size, size).stream()
                .map(p -> LikedProduct.of(
                        p.getId(),
                        p.getName(),
                        p.getMinPrice(),
                        p.getLikeCount(),
                        p.getStatus(),
                        p.getCreatedAt()
                ))
                .toList();
    }
}
