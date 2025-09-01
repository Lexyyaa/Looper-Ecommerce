package com.loopers.application.like;

import com.loopers.domain.like.LikeEvent;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeTargetType;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LikeEventListener Unit")
class LikeEventListenerUnitTest {

    @Mock
    private LikeService likeService;
    @Mock
    private ProductService productService;

    @InjectMocks
    LikeEventListener likeEventListener;

    @Test
    @DisplayName("[성공] Added 이벤트 처리 로직 호출")
    void success_handle_added_logic() {
        // arrange
        Long userId = 1L;
        Long targetId = 100L;
        var type = LikeTargetType.PRODUCT;

        var event = new LikeEvent.Added(userId, targetId, type);
        Long likeCnt = 5L;
        var product = Product.builder().id(targetId).status(Product.Status.ACTIVE).build();

        when(likeService.getLikeCount(targetId, type)).thenReturn(likeCnt);
        when(productService.getProduct(targetId)).thenReturn(product);

        // act
        likeEventListener.handle(event);

        // assert / verify
        verify(likeService, times(1)).getLikeCount(targetId, type);
        verify(productService, times(1)).getProduct(targetId);
        verify(productService, times(1)).updateLikeCnt(product, likeCnt);
    }

    @Test
    @DisplayName("[성공] Removed 이벤트 처리 로직 호출")
    void success_handle_removed_logic() {
        // arrange
        Long userId = 1L;
        Long targetId = 200L;
        var type = LikeTargetType.PRODUCT;

        var event = new LikeEvent.Removed(userId, targetId, type);
        Long likeCnt = 3L;
        var product = Product.builder().id(targetId).status(Product.Status.ACTIVE).build();

        when(likeService.getLikeCount(targetId, type)).thenReturn(likeCnt);
        when(productService.getProduct(targetId)).thenReturn(product);

        // act
        likeEventListener.handle(event);

        // assert / verify
        verify(likeService, times(1)).getLikeCount(targetId, type);
        verify(productService, times(1)).getProduct(targetId);
        verify(productService, times(1)).updateLikeCnt(product, likeCnt);
    }
}
