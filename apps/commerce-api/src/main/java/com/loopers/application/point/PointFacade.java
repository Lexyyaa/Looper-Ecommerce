package com.loopers.application.point;

import com.loopers.domain.user.*;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointFacade {

    private final UserService userService;

    public UserInfo.Point charge(UserCommand.Charge command){
        return userService.charge(command);
    }
}
