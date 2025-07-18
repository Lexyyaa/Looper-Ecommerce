package com.loopers.domain.user;

import lombok.Builder;

public class UserCommand {

    @Builder
    public record SignUp(
            String loginId,
            UserEntity.Gender gender,
            String name,
            String birth,
            String email
    ){
        public UserEntity toModel() {
            return new UserEntity(
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


