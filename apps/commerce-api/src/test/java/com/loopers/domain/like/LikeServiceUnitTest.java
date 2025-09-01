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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LikeService")
class LikeServiceUnitTest {

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private LikeService likeService;

    @Nested
    @DisplayName("[좋아요 저장]")
    class SaveLike {

        @Test
        @DisplayName("[성공] 사용자가 상품에 좋아요를 저장한다.")
        void success_saveLike() {
            likeService.save(1L, 100L, LikeTargetType.PRODUCT);
            verify(likeRepository).save(any(Like.class));
        }
    }

    @Nested
    @DisplayName("[좋아요 삭제]")
    class DeleteLike {

        @Test
        @DisplayName("[성공] 사용자가 기존 좋아요를 삭제한다.")
        void success_deleteLike() {
            Like like = Like.create(1L, 100L, LikeTargetType.PRODUCT);
            when(likeRepository.findLike(1L, 100L, LikeTargetType.PRODUCT)).thenReturn(Optional.of(like));

            likeService.delete(1L, 100L, LikeTargetType.PRODUCT);
            verify(likeRepository).delete(like);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 좋아요일 경우 BAD_REQUEST 예외가 발생한다.")
        void failure_deleteLike_whenLikeDoesNotExist() {
            when(likeRepository.findLike(1L, 100L, LikeTargetType.PRODUCT)).thenReturn(Optional.empty());

            CoreException ex = assertThrows(CoreException.class,
                    () -> likeService.delete(1L, 100L, LikeTargetType.PRODUCT));

            assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("[좋아요 개수 조회]")
    class GetLikeCount {

        @Test
        @DisplayName("[조회 성공] 상품의 좋아요 개수를 반환한다.")
        void success_getLikeCount() {
            when(likeRepository.countByTargetId(100L, LikeTargetType.PRODUCT)).thenReturn(5L);

            long count = likeService.getLikeCount(100L, LikeTargetType.PRODUCT);
            assertEquals(5L, count);
        }
    }

    @Nested
    @DisplayName("[좋아요한 상품 목록 조회]")
    class GetLikedProducts {

        @Test
        @DisplayName("[조회 성공] size=5, page=2일 경우 offset=10을 사용하여 상품을 조회한다.")
        void success_getLikedProducts_customPageSize() {
            LikedProductProjection projection = mock(LikedProductProjection.class);
            when(projection.getId()).thenReturn(21L);
            when(projection.getName()).thenReturn("상품21");

            // page=2, size=5 → offset = 10
            when(likeRepository.findLikedProducts(1L, 10, 5))
                    .thenReturn(List.of(projection));

            List<LikedProduct> result = likeService.getLikedProducts(1L, 2, 5);

            assertEquals(1, result.size());
            assertEquals("상품21", result.get(0).name());
        }

        @Test
        @DisplayName("[조회 성공] 좋아요한 상품이 없을 경우 빈 리스트를 반환한다.")
        void success_getLikedProducts_emptyList() {
            when(likeRepository.findLikedProducts(1L, 0, 10))
                    .thenReturn(Collections.emptyList());

            List<LikedProduct> result = likeService.getLikedProducts(1L, 0, 10);

            assertTrue(result.isEmpty());
        }
    }

}
