package com.cluster.platform.redis.core;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.JedisClusterInfoCache;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Advance
 * @date 2022年03月21日 14:50
 * @since V1.0.0
 */
class PipeCallbackForHash<K, H, V> extends BaseCallbackForHash<K, H, V> implements RedisCallback<V> {

    /**
     * write to redis or not
     */
    private RedisOperation operation;

    /**
     * data List for write or read or delete
     */
    private Map<K, Map<H, V>> dataList;

    /**
     * @param redisTemplate redisTemplate
     * @param dataMap data map for write or read or delete
     * @param operation {@link RedisOperation}
     * @param cache for cluster pipeline {@link JedisClusterInfoCache}
     */
    <T> PipeCallbackForHash(RedisTemplate<K, Map<H, V>> redisTemplate, //
                            Map<K, Map<H, V>> dataList, //
                            RedisOperation operation) {
        super(redisTemplate);
        this.operation = operation;
        this.dataList = dataList == null ? new HashMap<>() : dataList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V doInRedis(RedisConnection connection) {
        // 设置一条hash
        if (is(RedisOperation.HSET)) {
            doRedis((key, field, value) -> connection.hSet(rawKey(key), rawHashKey(field),
                    rawHashVal(value)));
        }
        // 取得一条Hash
        else if (is(RedisOperation.HGET)) {
            doRedis((key, field, value) -> connection.hGet(rawKey(key), rawHashKey(field)));
        }
        // 删除key对应的一条Hash
        else if (is(RedisOperation.HDEL)) {
            doRedis((key, field, value) -> connection.hDel(rawKey(key), rawHashKey(field)));
        }
        // 取得key对应的整个hash
        else if (is(RedisOperation.HGETALL)) {
            doRedis((key, field, value) -> connection.hGetAll(rawKey(key)));
        }
        // 删除key对应的整个hash
        else if (is(RedisOperation.DEL)) {
            doRedis((key, field, value) -> connection.del(rawKey(key)));
        }
        return null;
    }

    /**
     * 循环处理数据
     *
     * @param fun callback
     * @see
     * @since 1.0
     */
    private void doRedis(DoRedis<K, H, V> fun) {
        if (!CollectionUtils.isEmpty(dataList) && fun != null) {
            dataList.entrySet().forEach(hash -> {
                if (hash != null) {
                    K key = hash.getKey();
                    if (is(RedisOperation.HGETALL)) {
                        fun.doRedis(key, null, null);
                        return;
                    }
                    Map<H, V> map = hash.getValue();
                    map.entrySet().forEach(en -> {
                        H field = en.getKey();
                        V value = en.getValue();
                        fun.doRedis(key, field, value);
                    });
                }
            });
        }
    }

    protected boolean is(RedisOperation opr) {
        return operation == opr;
    }

    interface DoRedis<K, H, V> {

        void doRedis(K key, H field, V value);
    }
}

