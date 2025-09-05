package com.loopers.application.like;

import com.loopers.domain.like.*;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class LikeApplicationService implements LikeUsecase {

    private final UserService userService;
    private final LikeValidator likeValidator;
    private final LikeService likeService;
    private final ProductService productService;
    private final LikeEventPublisher likeEventPublisher;
    private final LikeActivityPublisher likeActivityPublisher;

    @Override
    @Transactional
    @CacheEvict(value = "product:detail", key = "#command.targetId")
    public void like(LikeCommand.Like command) {
        User user = userService.getUser(command.loginId());
        Product product = productService.getProduct(command.targetId());
        likeValidator.validateNotExists(user.getId(), product.getId(), command.targetType());
        likeService.save(user.getId(), product.getId(), command.targetType());

        // 사용자 활동로그(좋아요 등록)
        likeActivityPublisher.like(new LikeActivityPayload.LikeAdded(user.getId(), user.getLoginId(), product.getId(), LikeTargetType.PRODUCT));

        //좋아요 수 집계 이벤트 발행
        likeEventPublisher.like(new LikeEvent.Added(user.getId(), user.getLoginId(), product.getId(), LikeTargetType.PRODUCT));
    }

    @Override
    @Transactional
    @CacheEvict(value = "product:detail", key = "#command.targetId")
    public void unlike(LikeCommand.Like command) {

        User user = userService.getUser(command.loginId());
        Product product = productService.getProduct(command.targetId());
        likeValidator.validateExists(user.getId(), product.getId(), command.targetType());
        likeService.delete(user.getId(), product.getId(), command.targetType());

        // 사용자 활동로그(좋아요 취소)
        likeActivityPublisher.unlike(new LikeActivityPayload.LikeRemoved(user.getId(), user.getLoginId(), product.getId(), LikeTargetType.PRODUCT));

        //좋아요 수 집계 이벤트 발행
        likeEventPublisher.unlike(new LikeEvent.Removed(user.getId(), user.getLoginId(), product.getId(), command.targetType()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LikeInfo.LikedProduct> getLikedProducts(LikeCommand.LikedProducts command) {

        User user = userService.getUser(command.loginId());

        List<LikedProduct> infos = likeService.getLikedProducts(
                user.getId(),
                command.page(),
                command.size()
        );

        return infos.stream()
                .map(p -> new LikeInfo.LikedProduct(
                        p.id(),
                        p.name(),
                        p.price(),
                        p.likeCount(),
                        p.status(),
                        p.createdAt()
                ))
                .toList();
    }
}


