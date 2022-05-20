package com.cluster.platform.redis.data;

/**
 * Redis hash存储类型
 * @author Advance
 * @date 2022年03月21日 14:16
 * @since V1.0.0
 */
public class RedisHash<K, H, V> implements IRedisHash<K, H, V> {

    /**
     * 序列号
     */
    private static final long serialVersionUID = 2933938123164396371L;

    /**
     * 主键
     */
    private K key;

    /**
     * 域
     */
    private H field;

    /**
     * 值
     */
    private V value;

    /**
     * 获取 key
     *
     * @return key.
     */
    public K getKey() {
        return key;
    }

    /**
     * 设置 key
     *
     * @param key key
     */
    public void setKey(K key) {
        this.key = key;
    }

    /**
     * 获取 field
     *
     * @return field.
     */
    public H getField() {
        return field;
    }

    /**
     * 设置 field
     *
     * @param field field
     */
    public void setField(H field) {
        this.field = field;
    }

    /**
     * 获取 value
     *
     * @return value.
     */
    public V getValue() {
        return value;
    }

    /**
     * 设置 value
     *
     * @param value value
     */
    public void setValue(V value) {
        this.value = value;
    }
}

