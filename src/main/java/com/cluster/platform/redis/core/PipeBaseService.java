package com.cluster.platform.redis.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cluster.platform.redis.constants.RedisConstant;
import com.cluster.platform.redis.data.IRedisHash;
import com.cluster.platform.redis.data.RedisHash;
import com.cluster.platform.redis.util.RedisUtil;
import com.cluster.platform.redis.util.ProducerUtils;
import com.platform.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisClusterConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;

import com.cluster.platform.redis.msg.entity.MessageBody.Operation;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisClusterInfoCache;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSlotBasedConnectionHandler;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.util.JedisClusterCRC16;

/**
 * redis write or read or delete with pipeline for hash data
 * @author Advance
 * @date 2022年03月21日 14:04
 * @since V1.0.0
 */
abstract class PipeBaseService<K, H, V> {

    /**
     * field name connectionHandler {@link JedisSlotBasedConnectionHandler}
     */
    private static final String FIELD_CONNECTION_HANDLER = "connectionHandler";

    /**
     * field name cache {@link JedisClusterInfoCache}
     */
    private static final String FIELD_CACHE = "cache";

    /**
     * cluster
     */
    private JedisCluster cluster;

    /**
     * cluster cache for source
     */
    private JedisClusterInfoCache cache;

    /**
     * {@link RedisTemplate}
     */
    private RedisTemplate<K, Map<H, V>> redisTemplate;

    /**
     * 是否集群
     */
    private boolean isCluster;

    private static final int SCAN_COUNT = 10000;

    /**
     * 线程池
     */
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 项目值内容发布者
     */
    private ProducerUtils producer = ProducerUtils.getInstance();

    /**
     * 日志
     */
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * get the redisTemplate
     *
     * @return redisTemplate
     * @see
     * @since 1.0
     */
    protected RedisTemplate<K, Map<H, V>> redisTemplate() {
        return this.redisTemplate;
    }

    /**
     * get the all keys on the cluster nodes
     *
     * @param k the condition
     * @return List of the keys result
     * @see
     * @since 1.0
     */
    @SuppressWarnings("rawtypes")
    protected List<String> keys(String k) {
        final RedisSerializer keySerializer = redisTemplate.getKeySerializer();
        return redisTemplate.execute(new RedisCallback<List<String>>() {

            @Override
            public List<String> doInRedis(RedisConnection connection) {
                final List<byte[]> response = new ArrayList<>();
                String pattern = k == null ? RedisConstant.STAR : k;
                ScanParams params = new ScanParams();
                params.count(SCAN_COUNT);
                params.match(pattern);
                if (isCluster()) {
                    Map<String, JedisPool> poolMap = cluster.getClusterNodes();
                    if (!CollectionUtils.isEmpty(poolMap)) {
                        poolMap.entrySet().forEach(en -> {
                            JedisPool pool = en.getValue();
                            Jedis jedis = pool.getResource();
                            scanKey(response, jedis, params);
                            jedis.close();
                        });
                    }
                } else {
                    Jedis jedis = (Jedis) connection.getNativeConnection();
                    scanKey(response, jedis, params);
                }
                List<String> resList = new ArrayList<>();
                response.forEach(bt -> {
                    try {
                        resList.add((String) keySerializer.deserialize(bt));
                    } catch (Exception e) {
                        logger.error("反序列化异常{}", bt, e);
                    }
                });
                return resList;
            }
        });
    }

    private void scanKey(final List<byte[]> response, Jedis jedis, ScanParams params) {
        byte[] cursor = ScanParams.SCAN_POINTER_START_BINARY;
        while (true) {
            ScanResult<byte[]> result = jedis.scan(cursor, params);
            response.addAll(result.getResult());
            cursor = result.getCursorAsBytes();
            if (cursor[0] == ScanParams.SCAN_POINTER_START_BINARY[0]) {
                break;
            }
        }
    }

    /**
     * get the all keys on the cluster nodes
     *
     * @param k the condition
     * @return List of the keys result
     * @see
     * @since 1.0
     */
    protected List<String> hKeys(String k, String field) {
        return redisTemplate.execute(new RedisCallback<List<String>>() {

            @Override
            public List<String> doInRedis(RedisConnection connection) {
                try {
                    return hkeysDoInRedis(k, field, connection);
                } catch (Exception e) {
                    refreshCache();
                    return hkeysDoInRedis(k, field, connection);
                }
            }
        });
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected List<String> hkeysDoInRedis(String k, String field, RedisConnection connection){
        final RedisSerializer keySerializer = redisTemplate.getKeySerializer();
        final List<Map.Entry<byte[], byte[]>> response = new ArrayList<>();
        String pattern = field == null ? RedisConstant.STAR : field;
        byte[] kByte = keySerializer.serialize(k);
        ScanParams params = new ScanParams();
        params.count(SCAN_COUNT);
        params.match(pattern);
        if (isCluster()) {
            byte[] rawKey = keySerializer.serialize(k);
            // 根据Key值取得槽
            int slot = JedisClusterCRC16.getSlot(rawKey);
            // 又槽取得对应的redis节点
            JedisPool pool = cache.getSlotPool(slot);
            if (pool != null) {
                Jedis jedis = pool.getResource();
                scanHkey(response, kByte, jedis, params);
                jedis.close();
            }
        } else {
            Jedis jedis = (Jedis) connection.getNativeConnection();
            scanHkey(response, kByte, jedis, params);
        }
        List<String> resList = new ArrayList<>();
        response.forEach(bt -> resList.add((String) keySerializer.deserialize(bt.getKey())));
        return resList;
    }

    private void scanHkey(final List<Map.Entry<byte[], byte[]>> response, final byte[] k, Jedis jedis, ScanParams params) {
        byte[] cursor = ScanParams.SCAN_POINTER_START_BINARY;
        while (true) {
            ScanResult<Map.Entry<byte[], byte[]>> result = jedis.hscan(k, cursor, params);
            response.addAll(result.getResult());
            cursor = result.getCursorAsBytes();
            if (cursor[0] == ScanParams.SCAN_POINTER_START_BINARY[0]) {
                break;
            }
        }
    }

    /**
     * flush all data
     *
     * @see
     * @since 1.0
     */
    protected void flushAll() {
        redisTemplate.execute(new RedisCallback<String>() {

            @Override
            public String doInRedis(RedisConnection connection) {
                if (isCluster()) {
                    Map<String, JedisPool> poolMap = cluster.getClusterNodes();
                    if (!CollectionUtils.isEmpty(poolMap)) {
                        poolMap.entrySet().forEach(en -> {
                            JedisPool pool = en.getValue();
                            Jedis jedis = pool.getResource();
                            jedis.flushAll();
                            jedis.close();
                        });
                    }
                }
                else {
                    connection.flushAll();
                }
                return null;
            }
        });
    }

    /**
     * execute the function to do redis
     *
     * @param paramList data to write or for read
     * @param oper operation
     * @return result
     * @see
     * @since 1.0
     */
    protected List<Object> execute(Map<K, Map<H, V>> paramList, RedisOperation oper) {
        // 管道写Redis
        PipeCallbackForHash<K, H, V> calback = newPipeCallbackForHash(paramList, oper);
        // 集群时
        if (isCluster()) {
            try {
                return redisTemplate.execute(
                        new ClusterPipeCallbackForHash<>(redisTemplate, cache, paramList, calback));
            } catch (Exception e) {
                refreshCache();
                return redisTemplate.execute(
                        new ClusterPipeCallbackForHash<>(redisTemplate, cache, paramList, calback));
            }
        }
        else {
            return redisTemplate.executePipelined(calback);
        }
    }

    /**
     * 管道写hash数据
     *
     * @param paramList 数据
     * @see
     * @since 1.0
     */
    protected void pipeWriteForHash(Map<K, Map<H, V>> paramList) {
        execute(paramList, RedisOperation.HSET);
        // 发布消息
        /** publish(paramList, Operation.W); */
    }

    /**
     * 管道读hash数据
     *
     * @param paramList 参数
     * @return hash数据
     * @see
     * @since 1.0
     */
    protected Map<K, Map<H, V>> pipeReadForHash(Map<K, Map<H, V>> paramList) {
        List<Object> objList = execute(paramList, RedisOperation.HGET);
        if (StringUtil.isEmptyList(objList)) {
            return new HashMap<>();
        }
        // 发布消息
        /** publish(paramList, Operation.R); */
        // 构建返回值
        return returnMap(paramList, objList);
    }

    @SuppressWarnings("unchecked")
    private Map<K, Map<H, V>> returnMap(Map<K, Map<H, V>> paramList, List<Object> objList) {
        int index = 0;
        for (Map.Entry<K, Map<H, V>> hash : paramList.entrySet()) {
            Map<H, V> map = hash.getValue();
            for (Map.Entry<H, V> item : map.entrySet()) {
                H h = item.getKey();
                V v = (V)objList.get(index);
                map.put(h, v);
                index++ ;
            }
        }
        return paramList;
    }

    /**
     * publish the message
     *
     * @param paramList message body
     * @param opr operation type like write read delete
     * @see
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    protected void publish(Map<K, Map<H, V>> paramList, Operation opr) {
        if (!CollectionUtils.isEmpty(paramList)) {
            // 发布消息
            threadPoolTaskExecutor.execute(() -> paramList.entrySet().forEach(map -> {
                K k = map.getKey();
                map.getValue().entrySet().forEach(en -> {
                    RedisHash<K, H, V> hash = new RedisHash<>();
                    hash.setKey(k);
                    hash.setField(en.getKey());
                    hash.setValue(en.getValue());
                    producer.produce(opr, hash);
                });
            }));
        }
    }

    /**
     * read hash data from redsi by key with pipeline
     *
     * @param paramMap parameter which contains the key
     * @return hash data
     * @see
     * @since 1.0
     */
    @SuppressWarnings({"unchecked"})
    protected Map<K, Map<H, V>> pipeReadAllForHash(Map<K, Map<H, V>> paramList) {
        List<Object> objList = execute(paramList, RedisOperation.HGETALL);
        if (StringUtil.isEmptyList(objList)) {
            return new HashMap<>();
        }
        int index = 0;
        for (Map.Entry<K, Map<H, V>> hash : paramList.entrySet()) {
            K key = hash.getKey();
            Map<H, V> map = (Map<H, V>)objList.get(index);
            paramList.put(key, map);
            ++index;
        }
        // 发布消息
        /** publish(paramList, Operation.R); */
        return paramList;
    }

    /**
     * delete hash data from redis by key and hash key with pipeline
     *
     * @param paramList parameter which contains the key and hash key
     * @see
     * @since 1.0
     */
    protected void pipeDelForHash(Map<K, Map<H, V>> paramList) {
        execute(paramList, RedisOperation.HDEL);
        // 发布消息
        /** publish(paramList, Operation.D); */
    }

    protected void pipeDelAllForHash(Map<K, Map<H, V>> paramList) {
        execute(paramList, RedisOperation.DEL);
        // 发布消息
        /** publish(paramList, Operation.D); */
    }

    /**
     * @param dataMap data map for write or read or delete
     * @param operation {@link RedisOperation}
     */
    protected PipeCallbackForHash<K, H, V> newPipeCallbackForHash(Map<K, Map<H, V>> paramList, //
                                                                  RedisOperation operation) {
        return new PipeCallbackForHash<>(redisTemplate, paramList, operation);
    }

    protected boolean setIfAbsent(IRedisHash<K, H, V> param) {
        return redisTemplate.opsForHash().putIfAbsent(param.getKey(), param.getField(),
                param.getValue());
    }

    protected long delete(IRedisHash<K, H, V> param) {
        return redisTemplate.opsForHash().delete(param.getKey(), param.getField());
    }

    protected boolean hasKey(IRedisHash<K, H, V> param) {
        return redisTemplate.opsForHash().hasKey(param.getKey(), param.getField());
    }

    protected Cursor<Map.Entry<H, V>> scan(K key, int count, String pattern) {
        ScanOptions options = ScanOptions.scanOptions().count(count).match(pattern).build();
        HashOperations<K, H, V> hashOp = redisTemplate.opsForHash();
        Cursor<Map.Entry<H, V>> cursor = hashOp.scan(key, options);
        return cursor.open();
    }

    /**
     * 获取 redisTemplate
     *
     * @return redisTemplate.
     */
    public RedisTemplate<K, Map<H, V>> getRedisTemplate() {
        return redisTemplate;
    }

    /**
     * 设置 redisTemplate
     *
     * @param redisTemplate redisTemplate
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

        initCache();
    }

    /**
     * 初始化集群
     */
    protected void initCache(){
        try {
            /**
             * 解决集群不支持pipeline管道的问题
             */
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            if (connection instanceof JedisClusterConnection) {
                setCluster(true);
                JedisClusterConnection clusterConn = (JedisClusterConnection)connection;
                cluster = clusterConn.getNativeConnection();
                JedisSlotBasedConnectionHandler connectionHandler = RedisUtil.getFieldValue(
                        cluster, FIELD_CONNECTION_HANDLER);
                cache = RedisUtil.getFieldValue(connectionHandler, FIELD_CACHE);
            }
        }
        catch (Exception e) {
            /** getLogger().error("redis服务未启动", e.getMessage()); */
        }
    }

    /**
     * 刷新集群
     */
    protected void refreshCache(){
        if (cache == null) {
            return;
        }
        cache.renewClusterSlots(null);
    }

    protected Map<K, Map<H, V>> changeToMap(IRedisHash<K, H, V> hash) {
        Map<K, Map<H, V>> vList = new HashMap<>();
        Map<H, V> v = new HashMap<>();
        v.put(hash.getField(), hash.getValue());
        vList.put(hash.getKey(), v);
        return vList;
    }

    /**
     * 获取 cache
     *
     * @return cache.
     */
    public JedisClusterInfoCache getCache() {
        return cache;
    }

    /**
     * 设置 cache
     *
     * @param cache cache
     */
    public void setCache(JedisClusterInfoCache cache) {
        this.cache = cache;
    }

    /**
     * 获取 isCluster
     *
     * @return isCluster.
     */
    public boolean isCluster() {
        return isCluster;
    }

    /**
     * 设置 isCluster
     *
     * @param isCluster isCluster
     */
    public void setCluster(boolean isCluster) {
        this.isCluster = isCluster;
    }
}
