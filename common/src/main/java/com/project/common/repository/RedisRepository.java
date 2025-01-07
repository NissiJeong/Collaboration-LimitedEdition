package com.project.common.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public void saveData(String key, String value) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, value);
    }

    public String getData(String key) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    public boolean existData(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void setDataExpire(String key, String value, long duration) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key, value, expireDuration);
    }

    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    public void decrementData(String key, int orderQuantity) {
        redisTemplate.opsForValue().decrement(key, orderQuantity);
    }

    public void incrementData(String key, int orderQuantity) {
        redisTemplate.opsForValue().increment(key, orderQuantity);
    }

    public void saveListData(String key, List<String> values, Long duration, TimeUnit unit) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        listOps.rightPushAll(key, values);

        if(duration != null && unit != null)
            redisTemplate.expire(key, duration, unit);
    }

    public void extendExpire(String key, int time) {
        if (redisTemplate.hasKey(key)) {
            redisTemplate.expire(key, Duration.ofMinutes(time));
        } else {
            throw new IllegalArgumentException("예약 내역이 없습니다.");
        }
    }

    public List<String> getEntireList(String key) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        return listOps.range(key, 0, -1);
    }
}
