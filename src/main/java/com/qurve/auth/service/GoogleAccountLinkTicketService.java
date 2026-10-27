package com.qurve.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleAccountLinkTicketService {

    private static final String KEY_PREFIX = "auth:google-link:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private static final DefaultRedisScript<String> CONSUME_SCRIPT =
            new DefaultRedisScript<>("""
                    local value = redis.call('GET', KEYS[1])
                    if value then
                        redis.call('DEL', KEYS[1])
                    end
                    return value
                    """, String.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public record PendingLink(Long userId, String googleSub) {
    }

    public String issue(Long userId, String googleSub) {
        String ticket = UUID.randomUUID().toString();
        PendingLink pendingLink = new PendingLink(userId, googleSub);

        try {
            String value = objectMapper.writeValueAsString(pendingLink);
            redisTemplate.opsForValue().set(KEY_PREFIX + ticket, value, TTL);
            return ticket;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Google 계정 연결 요청을 생성하지 못했습니다.");
        }
    }

    public PendingLink load(String ticket) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + ticket);

        return decode(value);
    }

    public PendingLink consume(String ticket) {
        String value = redisTemplate.execute(CONSUME_SCRIPT, List.of(KEY_PREFIX + ticket));

        return decode(value);
    }

    private PendingLink decode(String value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.SOCIAL_LINK_INVALID);
        }

        try {
            PendingLink pendingLink = objectMapper.readValue(value, PendingLink.class);

            if (pendingLink == null
                    || pendingLink.userId() == null
                    || pendingLink.googleSub() == null
                    || pendingLink.googleSub().isBlank()) {
                throw new BusinessException(ErrorCode.SOCIAL_LINK_INVALID);
            }

            return pendingLink;
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.SOCIAL_LINK_INVALID);
        }
    }
}