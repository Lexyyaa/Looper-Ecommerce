package com.loopers.application.like;

import com.loopers.domain.like.*;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DisplayName("LikeApplicationService 통합 테스트")
class LikeApplicationServiceIntegrationTest {

    @Autowired
    LikeApplicationService likeApplicationService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    String loginId;
    Long activeProductId;

    @BeforeEach
    void setUp() {
        User user = User.create("user1", User.Gender.M, "사용자1","2020-02-20","xx@yy.zz",1000L);
        user = userRepository.save(user);
        loginId = user.getLoginId();

        Product active = Product.create("액티브상품", Product.Status.ACTIVE, 1L);
        active = productRepository.saveProduct(active);
        activeProductId = active.getId();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("[성공] 좋아요 등록 후 목록에서 조회된다.")
    void success_like_and_list() {
        var likeCommand = new LikeCommand.Like(loginId, activeProductId, LikeTargetType.PRODUCT);

        likeApplicationService.like(likeCommand);

        var listCommand = new LikeCommand.LikedProducts(loginId, LikeTargetType.PRODUCT, 0, 10);
        List<LikeInfo.LikedProduct> list = likeApplicationService.getLikedProducts(listCommand);
        assertThat(list).isNotEmpty();
        assertThat(list.stream().map(LikeInfo.LikedProduct::id)).contains(activeProductId);
    }

    @Test
    @DisplayName("[성공] 좋아요 취소 시 목록에서 사라진다.")
    void success_unlike() {
        likeApplicationService.like(new LikeCommand.Like(loginId, activeProductId, LikeTargetType.PRODUCT));

        likeApplicationService.unlike(new LikeCommand.Like(loginId, activeProductId, LikeTargetType.PRODUCT));

        var listCommand = new LikeCommand.LikedProducts(loginId, LikeTargetType.PRODUCT, 0, 10);
        List<LikeInfo.LikedProduct> list = likeApplicationService.getLikedProducts(listCommand);
        assertThat(list.stream().map(LikeInfo.LikedProduct::id)).doesNotContain(activeProductId);
    }

    @Test
    @DisplayName("[실패] 중복 좋아요 시 BAD_REQUEST")
    void failure_like_duplicate() {
        var cmd = new LikeCommand.Like(loginId, activeProductId, LikeTargetType.PRODUCT);
        likeApplicationService.like(cmd);

        CoreException ex = assertThrows(CoreException.class,
                () -> likeApplicationService.like(cmd));
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 좋아요 취소 시 BAD_REQUEST")
    void failure_unlike_not_exists() {
        CoreException ex = assertThrows(CoreException.class,
                () -> likeApplicationService.unlike(
                        new LikeCommand.Like(loginId, activeProductId, LikeTargetType.PRODUCT)));

        assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
    }

    @Test
    @DisplayName("[성공] 좋아요한 상품 목록 조회(페이지네이션 포함 최소 검증)")
    void success_getLikedProducts_pagination_min() {
        Product p2 = productRepository.saveProduct(Product.create("액티브상품2", Product.Status.ACTIVE, 1L));
        likeApplicationService.like(new LikeCommand.Like(loginId, activeProductId, LikeTargetType.PRODUCT));
        likeApplicationService.like(new LikeCommand.Like(loginId, p2.getId(), LikeTargetType.PRODUCT));

        var page0 = likeApplicationService.getLikedProducts(new LikeCommand.LikedProducts(loginId, LikeTargetType.PRODUCT, 0, 10));

        assertThat(page0).isNotEmpty();
        LikeInfo.LikedProduct any = page0.get(0);
        assertThat(any.createdAt()).isNotNull();
        assertThat(any.status()).isNotNull();
        assertThat(any.likeCount()).isGreaterThanOrEqualTo(0);

        var sized = likeApplicationService.getLikedProducts(new LikeCommand.LikedProducts(loginId, LikeTargetType.PRODUCT, 0, 1));
        assertThat(sized).hasSize(1);
    }
}
