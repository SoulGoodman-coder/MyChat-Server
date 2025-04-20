package com.mychat.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * projectName: com.mychat.redis
 * author:  SoulGoodman-coder
 * description: redis配置类
 */

@Configuration
public class RedisConfig<T> {
    @Bean("redisTemplate")
    public RedisTemplate<String, T> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, T> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // 设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        // 设置value的序列化方式
        template.setHashKeySerializer(RedisSerializer.json());
        // 设置hash的key的序列化方式
        template.setHashValueSerializer(RedisSerializer.string());
        // 设置hash的value的序列化方式
        template.setValueSerializer(RedisSerializer.json());
        template.afterPropertiesSet();
        return template;
    }
}
