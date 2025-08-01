package com.loopers.interfaces.api.controller.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserCommand;
import jakarta.validation.constraints.NotNull;

public class UserV1Request {

    public record SignUp(
            @NotNull
            String loginId,
            @NotNull
            User.Gender gender,
            @NotNull
            String name,
            @NotNull
            String birth,
            @NotNull
            String email
    ){
        public UserCommand.SignUp toCommand() {
            return new UserCommand.SignUp(
                    this.loginId,
                    this.gender,
                    this.name,
                    this.birth,
                    this.email
            );
        }
    }
}
