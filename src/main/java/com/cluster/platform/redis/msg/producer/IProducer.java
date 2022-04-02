package com.cluster.platform.redis.msg.producer;

import com.cluster.platform.redis.msg.entity.MessageBody;

/**
 * @author: Advance
 * @create: 2022-03-21 14:34
 * @since V1.0.0
 */
public interface IProducer {
    /**
     * 生产数据
     *
     * @param opr 操作类型
     * @param v 数据
     * @see
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    default <V> void produce(MessageBody.Operation opr, V... v) {
        if (v == null || v.length == 0) {
            return;
        }
        Class<?> vClz = v[0].getClass();
        String channel = vClz.getName();
        produce(channel, opr, v);
    }

    /**
     * 生产数据
     *
     * @param channel 频道
     * @param opr 操作类型
     * @param v 数据
     * @see
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    <V> void produce(String channel, MessageBody.Operation opr, V... v);
}
