package xenosoft.imldintelligence.module.identity.internal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xenosoft.imldintelligence.module.identity.internal.service.TokenBlacklistService;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private static final String KEY_PREFIX = "imld:token:blacklist:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void blacklist(String jti, Duration ttl) {
        if (jti == null || jti.isBlank()) {
            return;
        }
        Duration effectiveTtl = (ttl == null || ttl.isNegative() || ttl.isZero())
                ? Duration.ofMinutes(1) : ttl;
        redisTemplate.opsForValue().set(KEY_PREFIX + jti, "1", effectiveTtl);
    }

    @Override
    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + jti));
    }
}
