package com.loopers.domain.user;

import lombok.Builder;

public class UserCommand {

    @Builder
    public record SignUp(
            String loginId,
            User.Gender gender,
            String name,
            String birth,
            String email
    ){
        public User toModel() {
            return new User(
                    this.loginId,
                    this.gender,
                    this.name,
                    this.birth,
                    this.email
            );
        }
    }
    @Builder
    public record Charge(
            String loginId,
            Long amount
    ){
    }
}


