package com.qurve.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 관련 설정 클래스
 *
 * * 애플리케이션에서 Redis 서버와 연결하고,
 * Redis 데이터 저장/조회에 사용할 RedisTemplate Bean을 등록한다.
 *
 * * 주로 Refresh Token, 인증 정보 캐싱, 임시 데이터 저장 등
 * 빠른 조회가 필요한 기능에서 Redis를 사용하기 위해 설정한다.
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    /**
     * Redis 연결 팩토리 등록
     *
     * * Spring Data Redis가 Redis 서버와 연결할 때 사용할
     * 기본 ConnectionFactory 객체를 Bean으로 등록한다.
     *
     * @return Redis 연결 팩토리 객체
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    /**
     * RedisTemplate 등록
     *
     * * Redis 데이터 저장 및 조회 시 사용할 공통 템플릿 객체를 등록한다.
     *
     * * 기본 직렬화를 사용할 경우 key/value가 바이트 형태로 저장되어
     * Redis CLI에서 조회하기 어렵기 때문에 문자열 직렬화를 적용한다.
     *
     * @param connectionFactory Redis 연결 객체
     * @return 문자열 기반 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Redis CLI에서 사람이 읽을 수 있는 형태로 저장되도록 문자열 직렬화 사용
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }
}