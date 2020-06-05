package com.cluster.platform.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.nio.charset.Charset;
import java.util.*;

/**
 * 
 * @ClassName		: RedisRepository 
 * @author			: Reace
 * @date 			: 23点42分
 *
 */
public class RedisRepository implements ICache {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRepository.class);

    /**
     * 默认编码
     */
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    /**
     * Spring Redis Template
     */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public RedisRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 添加到带有 过期时间的  缓存
     *
     * @param key   redis主键
     * @param value 值
     * @param time  过期时间（秒）
     */
    public void setExpire(final byte[] key, final byte[] value, final long time) {
        redisTemplate.execute((RedisCallback<Long>) connection -> {
            connection.set(key, value);
            connection.expire(key, time);
            LOGGER.debug("[redisTemplate redis]放入 缓存  url:{} ========缓存时间为{}秒", key, time);
            return 1L;
        });
    }

    /**
     * 添加到带有 过期时间的  缓存
     *
     * @param key   redis主键
     * @param value 值
     * @param time  过期时间（秒）
     */
    @Override
    public <T> void setExpire(final String key, final T value, final long time) {
        redisTemplate.execute((RedisCallback<Long>) connection -> {
            RedisSerializer<String> keySerializer = getKeyRedisSerializer();
            RedisSerializer<T> valueSerializer = getValueRedisSerializer();
            byte[] keys = keySerializer.serialize(key);
            byte[] values = valueSerializer.serialize(value);
            connection.set(keys, values);
            connection.expire(keys, time);
            LOGGER.debug("[redisTemplate redis]放入 缓存  url:{} ========缓存时间为{}秒", key, time);
            return 1L;
        });
    }

    /**
     * 一次性添加数组带过期时间的数据到缓存，不用多次连接，节省开销
     *
     * @param keys   redis主键数组
     * @param values 值数组
     * @param time   过期时间（秒）
     */
    public <T> void setExpire(final String[] keys, final T[] values, final long time) {
        redisTemplate.execute((RedisCallback<Long>) connection -> {
            RedisSerializer<String> keySerializer = getKeyRedisSerializer();
            RedisSerializer<T> valueSerializer = getValueRedisSerializer();
            for (int i = 0; i < keys.length; i++) {
                byte[] bKeys = keySerializer.serialize(keys[i]);
                byte[] bValues = valueSerializer.serialize(values[i]);
                connection.set(bKeys, bValues);
                connection.expire(bKeys, time);
                LOGGER.debug("[redisTemplate redis]放入 缓存  url:{} ========缓存时间为:{}秒", keys[i], time);
            }
            return 1L;
        });
    }


    /**
     * 一次性添加数组到缓存，不用多次连接，节省开销
     *
     * @param keys   redis 主键数组
     * @param values 值数组
     */
    public <T> void set(final String[] keys, final T[] values) {
        redisTemplate.execute((RedisCallback<Long>) connection -> {
        	RedisSerializer<String> keySerializer = getKeyRedisSerializer();
            RedisSerializer<T> valueSerializer = getValueRedisSerializer();
            for (int i = 0; i < keys.length; i++) {
                byte[] bKeys = keySerializer.serialize(keys[i]);
                byte[] bValues = valueSerializer.serialize(values[i]);
                connection.set(bKeys, bValues);
                LOGGER.debug("[redisTemplate redis]放入 缓存  url:{}", keys[i]);
            }
            return 1L;
        });
    }


    /**
     * 添加数据到缓存
     *
     * @param key   redis主键
     * @param value 值
     */
	public <T> void set(final String key, final T value) {
        redisTemplate.execute((RedisCallback<Long>) connection -> {
        	RedisSerializer<String> keySerializer = getKeyRedisSerializer();
            RedisSerializer<T> valueSerializer = getValueRedisSerializer();
            byte[] keys = keySerializer.serialize(key);
            byte[] values = valueSerializer.serialize(value);
            connection.set(keys, values);
            LOGGER.debug("[redisTemplate redis]放入 缓存  url:{}", key);
            return 1L;
        });
    }

    /**
     * 查询在这个时间段内即将过期的key
     *
     * @param keyPattern key通配符  
     * @param time 时间段 （秒）
     * @return list 即将过期的Key集合
     */
    public List<String> willExpire(final String keyPattern, final long time) {
        final List<String> keysList = new ArrayList<>();
        redisTemplate.execute((RedisCallback<List<String>>) connection -> {
            Set<String> keys = redisTemplate.keys(keyPattern + "*");
            for (String key1 : keys) {
                Long ttl = connection.ttl(key1.getBytes(DEFAULT_CHARSET));
                if (0 <= ttl && ttl <= time) {
                    keysList.add(key1);
                }
            }
            return keysList;
        });
        return keysList;
    }


    /**
     * 查询在以keyPatten的所有  key
     *
     * @param keyPatten key通配符
     * @return set 匹配结果的Key Set
     */
    public Set<String> keys(final String keyPatten) {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> redisTemplate.keys(keyPatten + "*"));
    }

    /**
     * 根据key获取对象
     *
     * @param key redis主键
     * @return byte [ ]
     */
    public byte[] get(final byte[] key) {
        byte[] result = redisTemplate.execute((RedisCallback<byte[]>) connection -> connection.get(key));
        LOGGER.debug("[redisTemplate redis]取出 缓存  url:{} ", key);
        return result;
    }

    /**
     * 根据key获取对象
     *
     * @param key redis 主键
     * @return T 值
     */
    @Override
    public <T> T get(final String key) {
        T resultValue = redisTemplate.execute((RedisCallback<T>) connection -> {
        	RedisSerializer<String> keySerializer = getKeyRedisSerializer();
            RedisSerializer<T> valueSerializer = getValueRedisSerializer();
            byte[] keys = keySerializer.serialize(key);
            byte[] values = connection.get(keys);
            return valueSerializer.deserialize(values);
        });
        LOGGER.debug("[redisTemplate redis]取出 缓存  url:{} ", key);
        return resultValue;
    }


    /**
     * 根据key获取对象
     *
     * @param keyPatten key 通配符
     * @return Map<String, T> 匹配结果的键值对Map
     */
    public <T> Map<String, T> getKeysValues(final String keyPatten) {
        LOGGER.debug("[redisTemplate redis]  getValues()  patten={} ", keyPatten);
        return redisTemplate.execute((RedisCallback<Map<String, T>>) connection -> {
        	RedisSerializer<String> keySerializer = getKeyRedisSerializer();
            RedisSerializer<T> valueSerializer = getValueRedisSerializer();
            Map<String, T> maps = new HashMap<>();
            Set<String> keys = redisTemplate.keys(keyPatten + "*");
            for (String key : keys) {
                byte[] bKeys = keySerializer.serialize(key);
                byte[] bValues = connection.get(bKeys);
                T value = valueSerializer.deserialize(bValues);
                maps.put(key, value);
            }
            return maps;
        });
    }

    /**
     * Ops for hash hash operations.
     *
     * @return the hash operations
     */
    public <T> HashOperations<String, String, T> opsForHash() {
        return redisTemplate.opsForHash();
    }

    /**
     * redis Hash数据结构 : 对HashMap操作
     *
     * @param key       redis 主键
     * @param hashKey   hashkey
     * @param hashValue 值
     */
    @Override
    public <T> void putHashValue(String key, String hashKey, T hashValue) {
        LOGGER.debug("[redisTemplate redis]  putHashValue()  key={},hashKey={},hashValue={} ", key, hashKey, hashValue);
        opsForHash().put(key, hashKey, hashValue);
    }

    /**
     * redis Hash数据结构 : 获取单个field对应的值
     *
     * @param key     redis主键
     * @param hashKey hash key
     * @return T 	   值
     */
    @SuppressWarnings("unchecked")
	public <T> T getHashValues(String key, String hashKey) {
        LOGGER.debug("[redisTemplate redis]  getHashValues()  key={},hashKey={}", key, hashKey);
        return (T) opsForHash().get(key, hashKey);
    }

    /**
     * redis Hash数据结构 : 根据key值删除
     *
     * @param key      redis 主键
     * @param hashKeys hash key
     */
    public void delHashValues(String key, Object... hashKeys) {
        LOGGER.debug("[redisTemplate redis]  delHashValues()  key={}", key);
        opsForHash().delete(key, hashKeys);
    }

    /**
     * redis Hash数据结构 : key只匹配map
     *
     * @param key redis 主键
     * @return the hash value
     */
    @SuppressWarnings("unchecked")
	public <T> Map<String, T> getHashValue(String key) {
        LOGGER.debug("[redisTemplate redis]  getHashValue()  key={}", key);
        return (Map<String, T>) opsForHash().entries(key);
    }

    /**
     * redis Hash数据结构 : 批量添加
     *
     * @param key redis 主键
     * @param map map 值
     */
    public <HV> void putHashValues(String key, Map<String, HV> map) {
        opsForHash().putAll(key, map);
    }

    /**
     * 集合数量
     *
     * @return 集合数量
     */
    public long dbSize() {
        return redisTemplate.execute(RedisServerCommands::dbSize);
    }

    /**
     * 清空redis存储的数据
     *
     * @return 清理完成返回“ok”
     */
    public String flushDB() {
        return redisTemplate.execute((RedisCallback<String>) connection -> {
            connection.flushDb();
            return "ok";
        });
    }

    /**
     * 判断某个主键是否存在
     *
     * @param key 需要判断的key
     * @return 存在返回true，不存在返回false
     */
    public boolean exists(final String key) {
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> connection.exists(key.getBytes(DEFAULT_CHARSET)));
    }


    /**
     * 删除key
     *
     * @param keys 需要删除的key列表
     * @return 总共删除的key数量
     */
    @Override
    public long del(final String... keys) {
        return redisTemplate.execute((RedisCallback<Long>) connection -> {
            long result = 0;
            for (String key : keys) {
                result += connection.del(key.getBytes(DEFAULT_CHARSET));
            }
            return result;
        });
    }

    /**
     * 获取 KeyRedisSerializer
     * @param	
     * @return  RedisSerializer<String>
     */
    @SuppressWarnings("unchecked")
	protected RedisSerializer<String> getKeyRedisSerializer() {
        return (RedisSerializer<String>) redisTemplate.getKeySerializer();
    }
    
    /**
     * 获取 ValueRedisSerializer
     * @param	
     * @return  RedisSerializer<V>
     */
    @SuppressWarnings("unchecked")
	protected <V> RedisSerializer<V> getValueRedisSerializer() {
        return (RedisSerializer<V>) redisTemplate.getValueSerializer();
    }

    /**
     * 对某个主键对应的值加一,value值必须是全数字的字符串
     *
     * @param key the key
     * @return the long
     */
    public long incr(final String key) {
        return redisTemplate.execute((RedisCallback<Long>) connection -> {
            RedisSerializer<String> redisSerializer = getKeyRedisSerializer();
            return connection.incr(redisSerializer.serialize(key));
        });
    }

    /**
     * redis List 引擎
     *
     * @return list operations
     */
    public ListOperations<String, Object> opsForList() {
        return redisTemplate.opsForList();
    }

    /**
     * redis List数据结构 : 将一个或多个值 value 插入到列表 key 的表头
     *
     * @param key   redis 键
     * @param value 值
     * @return the long
     */
    public <T> Long leftPush(String key, T value) {
        return opsForList().leftPush(key, value);
    }

    /**
     * redis List数据结构 : 移除并返回列表 key 的头元素
     *
     * @param key redis 键
     * @return T 值
     */
    @SuppressWarnings("unchecked")
	public <T> T leftPop(String key) {
        return (T) opsForList().leftPop(key);
    }

    /**
     * redis List数据结构 :将一个或多个值 value 插入到列表 key 的表尾(最右边)。
     *
     * @param key   redis 键
     * @param value 值
     * @return the long
     */
    public <T> Long rightPush(String key, T value) {
        return opsForList().rightPush(key, value);
    }

    /**
     * redis List数据结构 : 移除并返回列表 key 的末尾元素
     *
     * @param key redis 键
     * @return T 值
     */
    @SuppressWarnings("unchecked")
	public <T> T rightPop(String key) {
        return (T) opsForList().rightPop(key);
    }


    /**
     * redis List数据结构 : 返回列表 key 的长度 ; 如果 key 不存在，则 key 被解释为一个空列表，返回 0 ; 如果 key 不是列表类型，返回一个错误。
     *
     * @param key redis 键
     * @return the long List 的大小
     */
    public Long length(String key) {
        return opsForList().size(key);
    }


    /**
     * redis List数据结构 : 根据参数 i 的值，移除列表中与参数 value 相等的元素
     *
     * @param key   the key
     * @param i     the
     * @param value the value
     */
    public void remove(String key, long i, Object value) {
        opsForList().remove(key, i, value);
    }

    /**
     * redis List数据结构 : 将列表 key 下标为 index 的元素的值设置为 value
     *
     * @param key   redis 键
     * @param index 
     * @param value 值
     */
    public <T> void set(String key, long index, T value) {
        opsForList().set(key, index, value);
    }

    /**
     * redis List数据结构 : 返回列表 key 中指定区间内的元素，区间以偏移量 start 和 end 指定。
     *
     * @param key   redis 键
     * @param start 开始位置
     * @param end   结束位置
     * @return the list
     */
    @SuppressWarnings("unchecked")
	public <T> List<T> getList(String key, int start, int end) {
        return (List<T>) opsForList().range(key, start, end);
    }

    /**
     * redis List数据结构 : 批量存储
     *
     * @param key  redis 键
     * @param list 值集合
     * @return the long
     */
    public <T> Long leftPushAll(String key, List<T> list) {
        return opsForList().leftPushAll(key, list);
    }

    /**
     * redis List数据结构 : 将值 value 插入到列表 key 当中，位于值 index 之前或之后,默认之后。
     *
     * @param key   redis 键
     * @param index 位置
     * @param value 值
     */
    public <T> void insertInList(String key, long index, T value) {
        opsForList().set(key, index, value);
    }
}
