package xenosoft.imldintelligence.module.identity.internal.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisTokenBlacklistServiceTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;

    @Test
    void blacklistWritesKeyWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        RedisTokenBlacklistService service = new RedisTokenBlacklistService(redisTemplate);
        service.blacklist("abc-123", Duration.ofMinutes(10));

        verify(valueOps).set("imld:token:blacklist:abc-123", "1", Duration.ofMinutes(10));
    }

    @Test
    void blacklistUsesMinimumTtlWhenZero() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        RedisTokenBlacklistService service = new RedisTokenBlacklistService(redisTemplate);
        service.blacklist("abc-456", Duration.ZERO);

        verify(valueOps).set("imld:token:blacklist:abc-456", "1", Duration.ofMinutes(1));
    }

    @Test
    void blacklistIgnoresBlankJti() {
        RedisTokenBlacklistService service = new RedisTokenBlacklistService(redisTemplate);
        service.blacklist(null, Duration.ofMinutes(5));
        service.blacklist("", Duration.ofMinutes(5));
        service.blacklist("  ", Duration.ofMinutes(5));

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void isBlacklistedReturnsTrueWhenKeyExists() {
        when(redisTemplate.hasKey("imld:token:blacklist:jti-1")).thenReturn(true);

        RedisTokenBlacklistService service = new RedisTokenBlacklistService(redisTemplate);
        assertThat(service.isBlacklisted("jti-1")).isTrue();
    }

    @Test
    void isBlacklistedReturnsFalseWhenKeyMissing() {
        when(redisTemplate.hasKey("imld:token:blacklist:jti-2")).thenReturn(false);

        RedisTokenBlacklistService service = new RedisTokenBlacklistService(redisTemplate);
        assertThat(service.isBlacklisted("jti-2")).isFalse();
    }

    @Test
    void isBlacklistedReturnsFalseForNull() {
        RedisTokenBlacklistService service = new RedisTokenBlacklistService(redisTemplate);
        assertThat(service.isBlacklisted(null)).isFalse();
        assertThat(service.isBlacklisted("")).isFalse();
    }
}
