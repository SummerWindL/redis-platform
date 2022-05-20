package com.cluster.platform.redis.common;

import java.io.Serializable;

/**
 * @author Advance
 * @date 2022年05月20日 16:05
 * @since V1.0.0
 */
public interface IValue<V> extends Serializable {

    /**
     * 设置项目值
     *
     * @param str 设置源字符串
     * @see
     * @since 1.0
     */
    void value(String str);

    /**
     * 取得项目值
     *
     * @return 项目值
     * @see
     * @since 1.0
     */
    V value();

    /**
     * 设置项目值
     *
     * @param v 项目值
     * @see
     * @since 1.0
     */
    void set(V v);
}
