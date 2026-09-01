package com.springtest.webchatsocket.repository;

import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RedisDao {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisDao(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void leftPushAndTrim(String key, String value, int maxSize) {
        stringRedisTemplate.opsForList().leftPush(key, value);
        stringRedisTemplate.opsForList().trim(key, 0, maxSize - 1L);
    }

    public List<String> range(String key, long start, long end) {
        List<String> values = stringRedisTemplate.opsForList().range(key, start, end);
        return values == null ? List.of() : values;
    }

}
