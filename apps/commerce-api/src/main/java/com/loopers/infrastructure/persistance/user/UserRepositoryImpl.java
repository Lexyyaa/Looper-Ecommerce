package com.loopers.infrastructure.persistance.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public Optional<User> findByLoginId(String loginId) {
        return jpaRepository.findbyLoginId(loginId);
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public boolean existsByLoginId(String loginId) {
        return jpaRepository.existsByLoginId(loginId);
    }

    @Override
    public Optional<User> findByLoginIdWithPessimisticLock(String loginId) {
        return jpaRepository.findByLoginIdWithPessimisticLock(loginId);
    }
}
