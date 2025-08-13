package com.loopers.application.like;

import com.loopers.domain.like.*;
import com.loopers.domain.product.*;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@DisplayName("LikeApplicationService 동시성 테스트")
class LikeConcurrencyTest {

    @Autowired
    LikeApplicationService likeApplicationService;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired UserRepository userRepository;

    Long productId;

    @BeforeEach
    void setUp() {
        Product product = Product.create("맥북프로", Product.Status.ACTIVE, 1L);
        product = productRepository.saveProduct(product);
        productId = product.getId();
    }

    private User createTestUser(String login, String email) {
        User user = User.create(login, User.Gender.M, "사용자-" + login, "2020-01-01", email, 0L);
        return userRepository.save(user);
    }

    @DisplayName("[동시성] 동일한 상품에 대해 여러명이 좋아요 요청해도, 좋아요 수가 정확히 증가한다")
    @Test
    void concurrency_like_cnt() throws InterruptedException {
        int userCount = 100;
        List<User> users = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            users.add(createTestUser("user" + i, "massive" + i + "@test.com"));
        }
        CountDownLatch latch = new CountDownLatch(userCount);
        AtomicInteger successCount = new AtomicInteger(0);

        try (ExecutorService executor = Executors.newFixedThreadPool(userCount)) {
            for (int i = 0; i < userCount; i++) {
                final String loginId = users.get(i).getLoginId();
                executor.submit(() -> {
                    try {
                        LikeCommand.Like cmd = new LikeCommand.Like(loginId, productId, LikeTargetType.PRODUCT);
                        likeApplicationService.like(cmd);
                        successCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        }

        long finalLikeCount = likeRepository.countByTargetId(productId, LikeTargetType.PRODUCT);
        assertThat(finalLikeCount).isEqualTo(userCount);
        assertThat(successCount.get()).isEqualTo(userCount);
    }

    @DisplayName("[동시성] 동일한 상품에 대해 여러명이 싫어요(취소) 요청해도, 최종 좋아요 수가 정확히 0이 된다")
    @Test
    void concurrency_unlike_cnt() throws InterruptedException {
        int userCount = 100;
        List<User> users = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            User u = createTestUser("u" + i, "u" + i + "@t.com");
            users.add(u);
            try {
                likeApplicationService.like(new LikeCommand.Like(u.getLoginId(), productId, LikeTargetType.PRODUCT));
            } catch (Exception ignore) {}
        }

        CountDownLatch latch = new CountDownLatch(userCount);
        AtomicInteger successCount = new AtomicInteger(0);

        try (ExecutorService executor = Executors.newFixedThreadPool(userCount)) {
            for (User u : users) {
                final String loginId = u.getLoginId();
                executor.submit(() -> {
                    try {
                        LikeCommand.Like cmd = new LikeCommand.Like(loginId, productId, LikeTargetType.PRODUCT);
                        likeApplicationService.unlike(cmd);
                        successCount.incrementAndGet();
                    } catch (Exception ignore) {

                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        }

        long finalLikeCount = likeRepository.countByTargetId(productId, LikeTargetType.PRODUCT);
        assertThat(finalLikeCount).isEqualTo(0L);
        assertThat(successCount.get()).isBetween(0, userCount);
    }
}



