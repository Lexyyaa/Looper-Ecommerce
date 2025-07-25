package com.loopers.interfaces.api.controller.point;


import com.loopers.domain.user.UserInfo;

public record PointV1Response(
        Long id,
        String loginId,
        String name,
        Long point
)
{
    public static PointV1Response from (UserInfo.Point info){
        return new PointV1Response(
                info.id(),
                info.loginId(),
                info.name(),
                info.point()
        );
    }
}
