package com.swp391.e_Motion_be.service.auth;

import com.swp391.e_Motion_be.entity.RedisToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisTokenService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PREFIX = "accessToken:";

    public void save(RedisToken token) {
        String key = PREFIX + token.getJwtId();
        redisTemplate.opsForValue().set(key, token, token.getExpiredTime(), TimeUnit.MILLISECONDS);
    }

    public RedisToken findById(String jwtId) {
        String key = PREFIX + jwtId;
        return (RedisToken) redisTemplate.opsForValue().get(key);
    }
}
