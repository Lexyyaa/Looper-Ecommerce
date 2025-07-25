package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserInfo.UserDetail signUp(UserCommand.SignUp command){
        boolean isExists = userRepository.existsByLoginId(command.loginId());
        UserEntity.validateUniqueLoginId(isExists);
        UserEntity user = userRepository.save(command.toModel());
        return UserInfo.UserDetail.from(user);
    }

    @Transactional(readOnly = true)
    public UserInfo.UserDetail myProfile(String loginId) {
        UserEntity user = userRepository.findByLoginId(loginId).orElse(null);
        return user == null ? null : UserInfo.UserDetail.from(user);
    }

    @Transactional
    public UserInfo.Point charge(UserCommand.Charge command){
        UserEntity currPoint = userRepository.findByLoginId(command.loginId()).orElseThrow(
                () -> new CoreException(ErrorType.NOT_FOUND, "해당 사용자를 찾을 수 없습니다.")
        );
        currPoint.charge(command.amount());
        UserEntity point = userRepository.save(currPoint);

        return UserInfo.Point.from(point);
    }

    @Transactional(readOnly = true)
    public UserInfo.Point myPoint(String loginId){
        UserEntity user = userRepository.findByLoginId(loginId).orElse(null);
        return user == null ? null : UserInfo.Point.from(user);
    }
}


