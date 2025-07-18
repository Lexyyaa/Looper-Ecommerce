package com.loopers.interfaces.api.controller.point;

import com.loopers.domain.user.UserCommand;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

public class PointV1Request {
    @Builder
    public record Charge(
            @NotNull
            Long amount
    ){
        public UserCommand.Charge toCommand(String loginId) {
            return new UserCommand.Charge(
                    loginId,
                    this.amount
            );
        }
    }
}
