package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loginId", nullable = false)
    private String loginId;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "name", nullable = false)
    private String  name;

    @Column(name = "birth", nullable = false)
    private String  birth;

    @Column(name = "email", nullable = false)
    private String  email;

    public enum Gender {
        M, F
    }

    public UserEntity(String loginId, Gender gender, String name, String birth, String email) {

        this.loginId = loginId;
        this.gender = gender;
        this.name = name;
        this.birth = birth;
        this.email = email;
    }
}
