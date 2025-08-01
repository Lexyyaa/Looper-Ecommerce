package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "target_id", "target_type"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Like extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private LikeTargetType  targetType;

    public static Like create(Long userId, Long targetId, LikeTargetType targetType) {
        return Like.builder()
                .userId(userId)
                .targetId(targetId)
                .targetType(targetType)
                .build();
    }

    public boolean isTargetOf(Long id, LikeTargetType type) {
        return this.targetId.equals(id) && this.targetType == type;
    }

    public boolean isSameUser(User user) {
        return this.userId.equals(user.getId());
    }
}
