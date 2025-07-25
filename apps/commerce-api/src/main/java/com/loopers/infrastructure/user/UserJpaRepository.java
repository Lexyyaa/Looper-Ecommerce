package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    @Query("SELECT u FROM UserEntity u WHERE u.loginId = :loginId")
    Optional<UserEntity> findbyLoginId(@Param("loginId") String loginId);

    boolean existsByLoginId(String loginId);
}
