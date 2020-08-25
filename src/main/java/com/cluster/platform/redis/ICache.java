package com.cluster.platform.redis;

import java.util.concurrent.TimeUnit;

/**
 * @author Reace
 * @createTime 05 22:31
 * @description
 */
public interface ICache {
    /**
     * 从缓存中取出数据
     * @param
     * @return  T
     */
    public <T> T get(final String key);

    /**
     * 把数据放入缓存
     * @param	key
     * @param	value
     * @param	time 数据过期时间（秒）
     * @return  void
     */
    public <T> void setExpire(final String key, final T value, final long time);

    /**
     * 根据key删除缓存数据
     * @param keys 需要删除的key列表
     * @return 总共删除的key数量
     */
    long del(String... keys);

    /**
     * redis Hash数据结构 : 对HashMap操作
     *
     * @param key       redis 主键
     * @param hashKey   hashkey
     * @param hashValue 值
     */
    public <T> void putHashValue(String key, String hashKey, T hashValue);


    public <T> void putOpsValue(String key, Integer v, Long l, TimeUnit t);

    public <T> T getOpsValues(String key);
}
