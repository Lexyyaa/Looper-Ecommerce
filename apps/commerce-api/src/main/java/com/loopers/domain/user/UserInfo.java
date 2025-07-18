package com.loopers.domain.user;

public class UserInfo {

    public record UserDetail (
            Long id,
            String loginId,
            UserEntity.Gender gender,
            String name,
            String birth,
            String email
    ) {
        public static UserDetail from (UserEntity model){
            return new UserDetail(
                    model.getId(),
                    model.getLoginId(),
                    model.getGender(),
                    model.getName(),
                    model.getBirth(),
                    model.getEmail()
            );
        }
    }

    public record Point(
            Long id,
            String loginId,
            String name,
            Long point
    ){
        public static Point from (UserEntity model){
            return new Point(
                    model.getId(),
                    model.getLoginId(),
                    model.getName(),
                    model.getPoint()
            );
        }
    }
}
