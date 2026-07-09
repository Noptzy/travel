package com.makeyourjurney.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;
    private final long refreshExpiryDays;

    public RefreshTokenStore(
            StringRedisTemplate redisTemplate,
            @Value("${app.jwt.refresh-expiry-days}") long refreshExpiryDays
    ) {
        this.redisTemplate = redisTemplate;
        this.refreshExpiryDays = refreshExpiryDays;
    }

    public String issue(String userId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(KEY_PREFIX + token, userId, Duration.ofDays(refreshExpiryDays));
        return token;
    }

    public Optional<String> resolve(String token) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + token));
    }

    public void revoke(String token) {
        redisTemplate.delete(KEY_PREFIX + token);
    }
}
