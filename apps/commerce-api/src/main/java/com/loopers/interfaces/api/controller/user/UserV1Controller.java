package com.loopers.interfaces.api.controller.user;

import com.loopers.application.user.UserFacade;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1ApiSpec {

    private final UserFacade userFacade;

    @PostMapping("")
    @Override
    public ApiResponse<UserV1Response> signUp(@RequestBody @Valid UserV1Request.SignUp request) {
        UserCommand.SignUp command = request.toCommand();
        UserV1Response response = UserV1Response.from(userFacade.signUp(command));
        return ApiResponse.success(response);
    }
}
