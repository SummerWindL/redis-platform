package com.cluster.platform.redis.msg.consumer;

import com.cluster.platform.redis.msg.entity.MessageBody;

import java.util.Collection;

/**
 * @author: Advance
 * @create: 2022-03-21 14:31
 * @since V1.0.0
 */
public interface IConsumer<V> {

    /**
     * 消费数据
     *
     * @param opr 操作类型
     * @param v 数据
     * @see
     * @since 1.0
     */
    void consume(MessageBody.Operation opr, Collection<V> vList);
}
