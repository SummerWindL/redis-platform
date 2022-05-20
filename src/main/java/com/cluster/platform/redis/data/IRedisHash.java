package com.cluster.platform.redis.data;

import java.io.Serializable;

/**
 * Redis hash data
 * @author: Advance
 * @create: 2022-03-21 14:15
 * @since V1.0.0
 */
public interface IRedisHash<K, F, V> extends Serializable {

    /**
     * get the Redis key
     *
     * @return Redis key
     * @see
     * @since 1.0
     */
    K getKey();

    /**
     * get the Redis hash key
     *
     * @return Redis hash key
     * @see
     * @since 1.0
     */
    F getField();

    /**
     * set the Redis hash key
     *
     * @param f Redis hash key
     * @see
     * @since 1.0
     */
    void setField(F f);

    /**
     * get the Redis hash value
     *
     * @return Redis hash value
     * @see
     * @since 1.0
     */
    V getValue();

    /**
     * set the Redis hash value
     *
     * @param v Redis hash value
     * @see
     * @since 1.0
     */
    void setValue(V v);
}

