package com.chenjun.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //缓存数据
    public void cacheData(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    //从缓存中获取数据
    public String getCacheData(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    //去重

    public boolean addUniqueRecord(String key, String value) {
        return stringRedisTemplate.opsForSet().add(key, value) > 0;
    }
}
