package com.loopers.application.like;

import com.loopers.domain.like.LikeCommand;
import com.loopers.domain.like.LikeInfo;

import java.util.List;

public interface LikeUsecase {
    void like(LikeCommand.Like command);
    void unlike(LikeCommand.Like command);
    List<LikeInfo.LikedProduct> getLikedProducts(LikeCommand.LikedProducts command);
}
