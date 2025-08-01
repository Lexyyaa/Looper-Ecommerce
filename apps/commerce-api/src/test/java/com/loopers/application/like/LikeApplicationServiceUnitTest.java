package com.loopers.application.like;

import com.loopers.domain.like.*;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LikeApplicationService")
class LikeApplicationServiceUnitTest {

    @Mock
    private UserService userService;
    @Mock
    private LikeValidator likeValidator;
    @Mock
    private LikeService likeService;
    @Mock
    private ProductService productService;

    @InjectMocks
    private LikeApplicationService likeApplicationService;

    @Nested
    @DisplayName("[좋아요 등록]")
    class LikeProduct {

        @Test
        @DisplayName("[성공] 사용자가 상품에 좋아요를 등록한다.")
        void success_likeProduct() {
            LikeCommand.Like cmd = new LikeCommand.Like("loginId", 100L, LikeTargetType.PRODUCT);
            User user = User.builder().id(1L).build();
            Product product = Product.builder().id(100L).status(Product.Status.ACTIVE).build();

            when(userService.getUser("loginId")).thenReturn(user);
            when(productService.getProduct(100L)).thenReturn(product);

            likeApplicationService.like(cmd);

            verify(likeValidator).validateNotExists(1L, 100L, LikeTargetType.PRODUCT);
            verify(likeService).save(1L, 100L, LikeTargetType.PRODUCT);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 사용자일 경우 NOT_FOUND 예외가 발생한다.")
        void failure_likeProduct_whenUserNotFound() {
            LikeCommand.Like cmd = new LikeCommand.Like("loginId", 100L, LikeTargetType.PRODUCT);
            when(userService.getUser("loginId")).thenThrow(new CoreException(ErrorType.NOT_FOUND, "사용자 없음"));

            CoreException ex = assertThrows(CoreException.class, () -> likeApplicationService.like(cmd));
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 상품일 경우 NOT_FOUND 예외가 발생한다.")
        void failure_likeProduct_whenProductNotFound() {
            LikeCommand.Like cmd = new LikeCommand.Like("loginId", 100L, LikeTargetType.PRODUCT);
            User user = User.builder().id(1L).build();
            when(userService.getUser("loginId")).thenReturn(user);
            when(productService.getProduct(100L)).thenThrow(new CoreException(ErrorType.NOT_FOUND, "상품 없음"));

            CoreException ex = assertThrows(CoreException.class, () -> likeApplicationService.like(cmd));
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        @Test
        @DisplayName("[실패] 이미 좋아요한 상품일 경우 BAD_REQUEST 예외가 발생한다.")
        void failure_likeProduct_whenAlreadyLiked() {
            LikeCommand.Like cmd = new LikeCommand.Like("loginId", 100L, LikeTargetType.PRODUCT);
            User user = User.builder().id(1L).build();
            Product product = Product.builder().id(100L).status(Product.Status.ACTIVE).build();
            when(userService.getUser("loginId")).thenReturn(user);
            when(productService.getProduct(100L)).thenReturn(product);
            doThrow(new CoreException(ErrorType.BAD_REQUEST, "이미 좋아요함"))
                    .when(likeValidator).validateNotExists(1L, 100L, LikeTargetType.PRODUCT);

            CoreException ex = assertThrows(CoreException.class, () -> likeApplicationService.like(cmd));
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("[좋아요 취소]")
    class UnLikeProduct {

        @Test
        @DisplayName("[성공] 사용자가 기존 좋아요를 취소한다.")
        void success_unlikeProduct() {
            LikeCommand.Like cmd = new LikeCommand.Like("loginId", 100L, LikeTargetType.PRODUCT);
            User user = User.builder().id(1L).build();
            Product product = Product.builder().id(100L).status(Product.Status.ACTIVE).build();
            when(userService.getUser("loginId")).thenReturn(user);
            when(productService.getProduct(100L)).thenReturn(product);

            likeApplicationService.unlike(cmd);

            verify(likeValidator).validateExists(1L, 100L, LikeTargetType.PRODUCT);
            verify(likeService).delete(1L, 100L, LikeTargetType.PRODUCT);
        }

        @Test
        @DisplayName("[실패] 좋아요하지 않은 상품일 경우 BAD_REQUEST 예외가 발생한다.")
        void failure_unlikeProduct_whenNotLiked() {
            LikeCommand.Like cmd = new LikeCommand.Like("loginId", 100L, LikeTargetType.PRODUCT);
            User user = User.builder().id(1L).build();
            Product product = Product.builder().id(100L).status(Product.Status.ACTIVE).build();
            when(userService.getUser("loginId")).thenReturn(user);
            when(productService.getProduct(100L)).thenReturn(product);
            doThrow(new CoreException(ErrorType.BAD_REQUEST, "좋아요 없음"))
                    .when(likeValidator).validateExists(1L, 100L, LikeTargetType.PRODUCT);

            CoreException ex = assertThrows(CoreException.class, () -> likeApplicationService.unlike(cmd));
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("[좋아요한 상품 목록 조회]")
    class GetLikedProducts {

        @Test
        @DisplayName("[성공] 사용자가 좋아요한 상품 목록을 반환한다.")
        void success_getLikedProducts() {
            LikeCommand.LikedProducts cmd = new LikeCommand.LikedProducts("loginId", LikeTargetType.PRODUCT, 1,5);
            User user = User.builder().id(1L).build();
            LikedProduct p = new LikedProduct(1L, "상품", 1000, 3L, Product.Status.ACTIVE, null);
            when(userService.getUser("loginId")).thenReturn(user);
            when(likeService.getLikedProducts(1L, 1, 5)).thenReturn(List.of(p));

            List<LikeInfo.LikedProduct> result = likeApplicationService.getLikedProducts(cmd);
            assertEquals(1, result.size());
            assertEquals("상품", result.get(0).name());
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 사용자일 경우 NOT_FOUND 예외가 발생한다.")
        void failure_getLikedProducts_whenUserNotFound() {
            LikeCommand.LikedProducts cmd = new LikeCommand.LikedProducts("loginId", LikeTargetType.PRODUCT, 1,5);
            when(userService.getUser("loginId")).thenThrow(new CoreException(ErrorType.NOT_FOUND, "사용자 없음"));

            CoreException ex = assertThrows(CoreException.class, () -> likeApplicationService.getLikedProducts(cmd));
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
