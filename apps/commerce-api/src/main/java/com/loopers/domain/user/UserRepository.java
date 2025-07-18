package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {
    UserEntity save(UserEntity user);
    boolean existsByLoginId(String loginId);
}

