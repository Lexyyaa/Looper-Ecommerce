package com.loopers.interfaces.api.controller.user;

import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.UserEntity;

public record UserV1Response(
        Long id,
        String loginId,
        UserEntity.Gender gender,
        String name,
        String birth,
        String email
)
{
    public static UserV1Response from (UserInfo.UserDetail info){
        return new UserV1Response(
                info.id(),
                info.loginId(),
                info.gender(),
                info.name(),
                info.birth(),
                info.email()
        );
    }
}

