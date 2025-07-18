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

    @Column(name = "point", nullable = true)
    private Long  point;

    public enum Gender {
        M, F
    }

    public UserEntity(String loginId, Gender gender, String name, String birth, String email) {

        if (loginId == null || !loginId.matches("^[a-zA-Z0-9]{1,10}$")) {
            throw new CoreException(
                    ErrorType.BAD_REQUEST,"ID는 영문 및 숫자 10자 이내여야 합니다."
            );
        }
        if (gender == null) {
            throw new CoreException(
                    ErrorType.BAD_REQUEST,"성별은 필수값 입니다."
            );
        }
        if (birth == null || !birth.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new CoreException(
                    ErrorType.BAD_REQUEST,"생년월일은 yyyy-MM-dd 형식이어야 합니다."
            );
        }
        if (email == null || !email.matches("^[^@\\s]+@[a-zA-Z0-9-]+\\.[a-zA-Z]{2,6}$")) {
            throw new CoreException(
                    ErrorType.BAD_REQUEST,"이메일 형식이 올바르지 않습니다."
            );
        }

        this.loginId = loginId;
        this.gender = gender;
        this.name = name;
        this.birth = birth;
        this.email = email;
        this.point = 0L;
    }

    public UserEntity(String loginId, Gender gender, String name, String birth, String email, Long point) {
        this.loginId = loginId;
        this.gender = gender;
        this.name = name;
        this.birth = birth;
        this.email = email;
        this.point = point;
    }

    public static void validateUniqueLoginId(boolean exists) {
        if(exists){
            throw new CoreException(ErrorType.BAD_REQUEST,"이미 존재하는 ID 입니다.");
        }
    }

    public void charge(Long amount){
        if(amount < 0){
            throw new CoreException(ErrorType.BAD_REQUEST,"충전 금액은 0 이상이어야 합니다.");
        }
        this.point += amount;
    }
}
