package com.loopers.domain.product;

public class CatalogMessage {

    public record LikeAdded(
            String loginId,
            Long targetId
    )  {}

    public record LikeRemoved(
            String loginId,
            Long targetId
    )  {}
}
