package com.imooc.seckill.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Redis分布式锁的实现
 * @author : wang zns
 * @date : 2019-05-10
 */
@Component
@Slf4j
public class RedisLock {


    @Autowired
    private StringRedisTemplate redisTemplate;


    /**
     * 加锁
     * @param key    seckillId
     * @param value  当前时间+超时时间(上锁喉超过该时间就自动释放锁)
     * @return
     */
    public boolean lock(String key, String value) {
        // 可以设置返回true
        //键不存在则新增,存在则不改变已经有的值。
        //setnx(key,value),如果为true，说明key和value设置到redlis里面了
        Boolean isLock = redisTemplate.opsForValue().setIfAbsent(key, value);
        if (isLock) {
            return true;
        }
        String currentValue = redisTemplate.opsForValue().get(key);
        // 如果锁已经过期
        if (!StringUtils.isEmpty(currentValue)
                && Long.valueOf(currentValue) < System.currentTimeMillis()) {
            // 获取上一个锁的时间，并设置新锁的时间
            String oldValue = redisTemplate.opsForValue().getAndSet(key, value);
            if (!StringUtils.isEmpty(oldValue)
                    && oldValue.equals(currentValue)) {
                log.info("锁过期并返回true");
                return true;
            }
        }
        return false;
    }

    /**
     * 解锁
     * @param key
     * @return
     */
    public void unlock(String key, String value) {
        try {
            String currentValue = redisTemplate.opsForValue().get(key);
            if (!StringUtils.isEmpty(currentValue)
                    && currentValue.equals(value)) {
                redisTemplate.opsForValue().getOperations().delete(key);
            }
        } catch (Exception e) {
            log.error("redis分布式锁，解锁异常, {}",e.getMessage());
        }

    }




}
