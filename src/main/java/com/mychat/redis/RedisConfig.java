package com.mychat.redis;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
public class RedisConfig<T> {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private Integer redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

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

    // 连接redisson
    @Bean(name = "redissonClient", destroyMethod = "shutdown")
    public RedissonClient redissonClient(){
        try {
            Config config = new Config();
            config.useSingleServer().setAddress("redis://" + redisHost + ":" + redisPort.toString()).setPassword(redisPassword);
            RedissonClient redissonClient = Redisson.create(config);
            return redissonClient;
        }catch (Exception e){
            log.error("redis配置错误，连接redisson失败");
            log.error(e.getMessage());
        }
        return null;
    }
}
