package com.loopers.domain.like;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LikeValidator")
class LikeValidatorTest {

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private LikeValidator likeValidator;

    @Nested
    @DisplayName("[좋아요 없음 검증]")
    class ValidateNotExists {

        @Test
        @DisplayName("[성공] 좋아요가 존재하지 않으면 예외가 발생하지 않는다.")
        void success_validateNotExists() {
            when(likeRepository.findLike(1L, 100L, LikeTargetType.PRODUCT)).thenReturn(Optional.empty());
            assertDoesNotThrow(() -> likeValidator.validateNotExists(1L, 100L, LikeTargetType.PRODUCT));
        }

        @Test
        @DisplayName("[실패] 이미 좋아요한 상품일 경우 BAD_REQUEST 예외가 발생한다.")
        void failure_validateNotExists_whenAlreadyLiked() {
            when(likeRepository.findLike(1L, 100L, LikeTargetType.PRODUCT))
                    .thenReturn(Optional.of(Like.create(1L, 100L, LikeTargetType.PRODUCT)));

            CoreException ex = assertThrows(CoreException.class,
                    () -> likeValidator.validateNotExists(1L, 100L, LikeTargetType.PRODUCT));

            assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("[좋아요 존재 검증]")
    class ValidateExists {

        @Test
        @DisplayName("[성공] 좋아요가 존재하면 예외가 발생하지 않는다.")
        void success_validateExists() {
            when(likeRepository.findLike(1L, 100L, LikeTargetType.PRODUCT))
                    .thenReturn(Optional.of(Like.create(1L, 100L, LikeTargetType.PRODUCT)));

            assertDoesNotThrow(() -> likeValidator.validateExists(1L, 100L, LikeTargetType.PRODUCT));
        }

        @Test
        @DisplayName("[실패] 좋아요가 존재하지 않으면 BAD_REQUEST 예외가 발생한다.")
        void failure_validateExists_whenNotLiked() {
            when(likeRepository.findLike(1L, 100L, LikeTargetType.PRODUCT)).thenReturn(Optional.empty());

            CoreException ex = assertThrows(CoreException.class,
                    () -> likeValidator.validateExists(1L, 100L, LikeTargetType.PRODUCT));

            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
