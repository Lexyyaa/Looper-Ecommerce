package com.loopers.redisexample;

import com.fasterxml.jackson.core.type.TypeReference;
import com.loopers.util.redis.JsonRedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;


@Service
@RequiredArgsConstructor
public class RedisMemberService {

    private final RedisMemberRepository redisMemberRepository;

    public void saveMember(String key, Member member, Duration ttl) {
        redisMemberRepository.saveMember(key, member, ttl);
    }

    public Member findMember(String key) {
        return redisMemberRepository.findMember(key);
    }

    public void saveMembers(String key, List<Member> members, Duration ttl) {
        redisMemberRepository.saveMembers(key, members, ttl);
    }

    public List<Member> findMembers(String key) {
        return redisMemberRepository.findMembers(key);
    }
}
