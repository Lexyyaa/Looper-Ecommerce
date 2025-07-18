package com.loopers.application.user;

import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserFacade {

    private final UserService userService;

    public UserInfo.UserDetail signUp(UserCommand.SignUp command){
        return userService.signUp(command);
    }

    public UserInfo.UserDetail myProfile(String loginId){
        UserInfo.UserDetail userDetail = userService.myProfile(loginId);

        if(userDetail == null){
            throw new CoreException(ErrorType.NOT_FOUND,"존재하지 않는 사용자 압니다");
        }
        return userDetail;
    }
}
