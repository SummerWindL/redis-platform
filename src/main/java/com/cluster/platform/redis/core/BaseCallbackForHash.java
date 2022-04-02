package com.cluster.platform.redis.core;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Map;

/**
 * @author Advance
 * @date 2022年03月21日 14:50
 * @since V1.0.0
 */
abstract class BaseCallbackForHash<K, H, V> {

    private RedisTemplate<K, Map<H, V>> redisTemplate;

    @SuppressWarnings("rawtypes")
    private RedisSerializer keySerializer;

    @SuppressWarnings("rawtypes")
    private RedisSerializer valueSerializer;

    @SuppressWarnings("rawtypes")
    private RedisSerializer hashKeySerializer;

    @SuppressWarnings("rawtypes")
    private RedisSerializer hashValSerializer;

    /**
     * @param redisTemplate redisTemplate
     */
    protected BaseCallbackForHash(RedisTemplate<K, Map<H, V>> redisTemplate) {
        this.keySerializer = redisTemplate.getKeySerializer();
        this.hashKeySerializer = redisTemplate.getHashKeySerializer();
        this.hashValSerializer = redisTemplate.getHashValueSerializer();
        this.valueSerializer = redisTemplate.getValueSerializer();
        this.redisTemplate = redisTemplate;
    }

    @SuppressWarnings("unchecked")
    protected byte[] rawKey(K key) {
        return keySerializer.serialize(key);
    }

    @SuppressWarnings("unchecked")
    protected byte[] rawHashKey(H hashKey) {
        return hashKeySerializer.serialize(hashKey);
    }

    @SuppressWarnings("unchecked")
    protected byte[] rawHashVal(V hashVal) {
        return hashValSerializer.serialize(hashVal);
    }

    /**
     * 获取 keySerializer
     *
     * @return keySerializer.
     */
    @SuppressWarnings("rawtypes")
    public RedisSerializer getKeySerializer() {
        return keySerializer;
    }

    /**
     * 获取 valueSerializer
     *
     * @return valueSerializer.
     */
    @SuppressWarnings("rawtypes")
    public RedisSerializer getValueSerializer() {
        return valueSerializer;
    }

    /**
     * 获取 hashKeySerializer
     *
     * @return hashKeySerializer.
     */
    @SuppressWarnings("rawtypes")
    public RedisSerializer getHashKeySerializer() {
        return hashKeySerializer;
    }

    /**
     * 获取 hashValSerializer
     *
     * @return hashValSerializer.
     */
    @SuppressWarnings("rawtypes")
    public RedisSerializer getHashValSerializer() {
        return hashValSerializer;
    }

    protected RedisTemplate<K, Map<H, V>> getRedisTemplate() {
        return redisTemplate;
    }
}

