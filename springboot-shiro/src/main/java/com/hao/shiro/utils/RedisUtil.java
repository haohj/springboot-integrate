package com.hao.shiro.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    @Autowired
    StringRedisTemplate redisTemplate;

    public void set(String tokenKey, String token) {
        redisTemplate.opsForValue().set(tokenKey, token);
    }

    public void expire(String tokenKey, long timeout) {
        redisTemplate.expire(tokenKey, timeout, TimeUnit.SECONDS);
    }

    public String get(String tokenKey) {
        return redisTemplate.opsForValue().get(tokenKey);
    }
}
