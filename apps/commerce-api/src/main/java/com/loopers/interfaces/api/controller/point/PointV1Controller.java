package com.loopers.interfaces.api.controller.point;

import com.loopers.application.point.PointFacade;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class PointV1Controller implements PointV1ApiSpec {

    private final PointFacade pointFacade;

    @PostMapping("/charge")
    @Override
    public ApiResponse<PointV1Response> charge(
            @RequestHeader("X-USER-ID") String loginId,
            @RequestBody PointV1Request.Charge request) {

        UserCommand.Charge command = request.toCommand(loginId);
        PointV1Response response = PointV1Response.from(pointFacade.charge(command));
        return ApiResponse.success(response);
    }
}
