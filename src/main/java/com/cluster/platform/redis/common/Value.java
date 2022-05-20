package com.cluster.platform.redis.common;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author Advance
 * @date 2022年05月20日 16:06
 * @since V1.0.0
 */
public class Value<V> implements IValue<V> {

    /**
     * 序列号
     */
    private static final long serialVersionUID = 4195349748977309776L;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private V info;

    private transient StringToV<V> fun;

    public Value(StringToV<V> fun) {
        this.fun = fun;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void value(String str) {
        if (fun != null) {
            info = fun.strToV(str);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V value() {
        return info;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(V info) {
        this.info = info;
    }

    public interface StringToV<V> {

        V strToV(String str);
    }
}

