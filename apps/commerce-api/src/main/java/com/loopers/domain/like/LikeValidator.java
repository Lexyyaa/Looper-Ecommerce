package com.loopers.domain.like;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeValidator {

    private final LikeRepository likeRepository;

    public void validateNotExists(Long userId, Long targetId, LikeTargetType targetType) {
        boolean exists = likeRepository.findLike(userId, targetId, targetType).isPresent();

        if (exists) {
            throw new CoreException(ErrorType.BAD_REQUEST,"이미 좋아요한 대상입니다.");
        }
    }
    public void validateExists(Long userId, Long targetId, LikeTargetType targetType) {
        boolean exists = likeRepository.findLike(userId, targetId, targetType).isPresent();

        if (!exists) {
            throw new CoreException(ErrorType.NOT_FOUND,"해당 좋아요가 존재하지 않습니다. ");
        }
    }
}
