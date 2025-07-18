package com.loopers.domain.user;

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
}


