package com.loopers.redisexample;


import com.fasterxml.jackson.core.type.TypeReference;
import com.loopers.util.redis.JsonRedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RedisMemberRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public void saveMember(String key, Member member, Duration ttl){
        String json = JsonRedisUtils.toJson(member);
        redisTemplate.opsForValue().set(key, json, ttl);
    }

    public Member findMember(String key) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        return JsonRedisUtils.fromJson(json, Member.class);
    }

    public void saveMembers(String key, List<Member> members, Duration ttl) {
        String json = JsonRedisUtils.toJson(members);
        redisTemplate.opsForValue().set(key, json, ttl);
    }

    public List<Member> findMembers(String key) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        return JsonRedisUtils.fromJson(json, new TypeReference<List<Member>>() {});
    }
}
