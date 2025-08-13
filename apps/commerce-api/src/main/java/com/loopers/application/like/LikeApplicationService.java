package com.loopers.application.like;

import com.loopers.domain.like.*;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class LikeApplicationService implements LikeUsecase {

    private final UserService userService;
    private final LikeValidator likeValidator;
    private final LikeService likeService;
    private final ProductService productService;

    @Override
    @Transactional
    public void like(LikeCommand.Like command) {
        User user = userService.getUser(command.loginId());
        Product product = productService.getProduct(command.targetId());
        likeValidator.validateNotExists(user.getId(), product.getId(), command.targetType());
        likeService.save(user.getId(), product.getId(), command.targetType());
    }

    @Override
    @Transactional
    public void unlike(LikeCommand.Like command) {
        User user = userService.getUser(command.loginId());
        Product product = productService.getProduct(command.targetId());
        likeValidator.validateExists(user.getId(), product.getId(), command.targetType());
        likeService.delete(user.getId(), product.getId(), command.targetType());
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


