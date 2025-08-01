package com.loopers.domain.like;

import com.loopers.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Like Entity")
class LikeEntityTest {

    @Nested
    @DisplayName("[Like 생성]")
    class CreateLike {

        @Test
        @DisplayName("[성공] 정상적인 정보로 Like를 생성한다.")
        void success_createLike() {
            Like like = Like.create(1L, 100L, LikeTargetType.PRODUCT);

            assertEquals(1L, like.getUserId());
            assertEquals(100L, like.getTargetId());
            assertEquals(LikeTargetType.PRODUCT, like.getTargetType());
        }
    }

    @Nested
    @DisplayName("[타겟 일치 여부 확인]")
    class IsTargetOf {

        @Test
        @DisplayName("[성공] 타겟 ID와 타입이 모두 일치하면 true를 반환한다.")
        void success_isTargetOf() {
            Like like = Like.create(1L, 100L, LikeTargetType.PRODUCT);

            assertTrue(like.isTargetOf(100L, LikeTargetType.PRODUCT));
        }

        @Test
        @DisplayName("[실패] 타겟 ID 또는 타입이 다르면 false를 반환한다.")
        void failure_isTargetOf_whenDifferent() {
            Like like = Like.create(1L, 100L, LikeTargetType.PRODUCT);

            assertFalse(like.isTargetOf(101L, LikeTargetType.PRODUCT));
            assertFalse(like.isTargetOf(100L, LikeTargetType.BRAND));
        }
    }

    @Nested
    @DisplayName("[동일 사용자 여부 확인]")
    class IsSameUser {

        @Test
        @DisplayName("[성공] 동일한 사용자일 경우 true를 반환한다.")
        void success_isSameUser() {
            User user = User.builder().id(1L).build();
            Like like = Like.create(1L, 100L, LikeTargetType.PRODUCT);

            assertTrue(like.isSameUser(user));
        }

        @Test
        @DisplayName("[실패] 다른 사용자일 경우 false를 반환한다.")
        void failure_isSameUser_whenDifferentUser() {
            User user = User.builder().id(2L).build();
            Like like = Like.create(1L, 100L, LikeTargetType.PRODUCT);

            assertFalse(like.isSameUser(user));
        }
    }
}


