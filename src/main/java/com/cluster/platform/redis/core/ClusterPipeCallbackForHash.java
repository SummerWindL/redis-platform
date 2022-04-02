package com.cluster.platform.redis.core;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationUtils;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClusterInfoCache;
import redis.clients.jedis.JedisPool;
import redis.clients.util.JedisClusterCRC16;

import java.util.*;

/**
 * @author Advance
 * @date 2022年03月21日 14:52
 * @since V1.0.0
 */
class ClusterPipeCallbackForHash<K, H, V> extends BaseCallbackForHash<K, H, V> implements RedisCallback<List<Object>> {

    /**
     * cluster cache for source
     */
    private final JedisClusterInfoCache cache;

    /**
     * callback action
     */
    private RedisCallback<V> action;

    private Map<JedisPool, Map<K, Map<H, V>>> exeMap;

    /**
     * @param redisTemplate redisTemplate
     * @param operation {@link RedisOperation}
     * @param dataList data to save
     * @param cache for cluster pipeline {@link JedisClusterInfoCache}
     */
    ClusterPipeCallbackForHash(RedisTemplate<K, Map<H, V>> redisTemplate, //
                               JedisClusterInfoCache cache, //
                               Map<K, Map<H, V>> dataList, //
                               RedisCallback<V> action) {
        super(redisTemplate);
        this.cache = cache;
        this.action = action;
        init(dataList);
    }

    protected void init(Map<K, Map<H, V>> dataList) {
        exeMap = new HashMap<>();
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }
        dataList.entrySet().forEach(hash -> {
            K k = hash.getKey();
            byte[] rawKey = rawKey(k);
            // 根据Key值取得槽
            int slot = JedisClusterCRC16.getSlot(rawKey);
            // 又槽取得对应的redis节点
            JedisPool pool = cache.getSlotPool(slot);
            Map<K, Map<H, V>> map = exeMap.getOrDefault(pool, new HashMap<>());
            map.put(k, hash.getValue());
            exeMap.put(pool, map);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> doInRedis(RedisConnection connection) {
        List<Object> resultList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(exeMap)) {
            exeMap.entrySet().forEach(en -> {
                JedisPool jedisPool = en.getKey();
                Jedis jedis = jedisPool.getResource();
                JedisConnection conn = new JedisConnection(jedis);
                conn.openPipeline();
                boolean pipelinedClosed = false;
                try {
                    Object result = action.doInRedis(conn);
                    if (result != null) {
                        throw new InvalidDataAccessApiUsageException(
                                "Callback cannot return a non-null value as it gets overwritten by the pipeline");
                    }
                    List<Object> closePipeline = conn.closePipeline();
                    pipelinedClosed = true;
                    resultList.addAll(deserializeMixedResults(closePipeline));
                }
                finally {
                    if (!pipelinedClosed) {
                        conn.closePipeline();
                    }
                    jedis.close();
                }
            });
        }
        return resultList;
    }

    private List<Object> deserializeMixedResults(List<Object> rawValues) {
        return deserializeMixedResults(rawValues, getValueSerializer(), getHashKeySerializer(),
                getHashValSerializer());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Object> deserializeMixedResults(List<Object> rawValues,
                                                 RedisSerializer valueSerializer,
                                                 RedisSerializer hashKeySerializer,
                                                 RedisSerializer hashValueSerializer) {
        if (rawValues == null) {
            return new ArrayList<>();
        }
        List<Object> values = new ArrayList<>();
        for (Object rawValue : rawValues) {
            if (rawValue instanceof byte[] && valueSerializer != null) {
                values.add(valueSerializer.deserialize((byte[])rawValue));
            }
            else if (rawValue instanceof List) {
                // Lists are the only potential Collections of mixed values....
                values.add(deserializeMixedResults((List)rawValue, valueSerializer,
                        hashKeySerializer, hashValueSerializer));
            }
            else if (rawValue instanceof Set && !(((Set)rawValue).isEmpty())) {
                values.add(deserializeSet((Set)rawValue, valueSerializer));
            }
            else if (rawValue instanceof Map && !(((Map)rawValue).isEmpty())
                    && ((Map)rawValue).values().iterator().next() instanceof byte[]) {
                values.add(SerializationUtils.deserialize((Map)rawValue, hashKeySerializer,
                        hashValueSerializer));
            }
            else {
                values.add(rawValue);
            }
        }
        return values;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Set<?> deserializeSet(Set rawSet, RedisSerializer valueSerializer) {
        if (rawSet.isEmpty()) {
            return rawSet;
        }
        Object setValue = rawSet.iterator().next();
        if (setValue instanceof byte[] && valueSerializer != null) {
            return (SerializationUtils.deserialize((Set)rawSet, valueSerializer));
        }
        else if (setValue instanceof RedisZSetCommands.Tuple) {
            return convertTupleValues(rawSet, valueSerializer);
        }
        else {
            return rawSet;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Set<ZSetOperations.TypedTuple<Map<H, V>>> convertTupleValues(Set<RedisZSetCommands.Tuple> rawValues,
                                                                         RedisSerializer valueSerializer) {
        Set<ZSetOperations.TypedTuple<Map<H, V>>> set = new LinkedHashSet<>(rawValues.size());
        for (RedisZSetCommands.Tuple rawValue : rawValues) {
            Object value = rawValue.getValue();
            if (valueSerializer != null) {
                value = valueSerializer.deserialize(rawValue.getValue());
            }
            set.add(new DefaultTypedTuple(value, rawValue.getScore()));
        }
        return set;
    }

}
