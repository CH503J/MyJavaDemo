package com.chenjun.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

//@Configuration
public class RedisConfiguration {
    /*
    * 建议自主配置RedisTemplate
    * 1.默认redisTemplate的序列化器是JdkSerializationRedisSerializer
    * 2.JdkSerializationRedisSerializer序列化器不支持null值 效率低 不安全 可读性比较差
    * 3.默认redisTemplate的key序列化器是StringRedisSerializer
    * 4.默认redisTemplate的value序列化器是JdkSerializationRedisSerializer
    * */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate();
        //配置key的链接方式(string方式)
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
}
