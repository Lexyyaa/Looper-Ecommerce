package com.loopers.domain.like;


import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class LikeCommand{

    public record Like(
            String loginId,
            Long targetId,
            LikeTargetType targetType
    ) {

        public static LikeCommand.Like of(String loginId, Long targetId, LikeTargetType targetType) {
            return new LikeCommand.Like(loginId, targetId, targetType);
        }
    }

    public record LikedProducts (
            String loginId,
            LikeTargetType targetType,
            int page,
            int size
    ){
        public Pageable toPageable() {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        public int offset() {
            return page * size;
        }
    }


}
