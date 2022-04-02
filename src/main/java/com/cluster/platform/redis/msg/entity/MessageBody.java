package com.cluster.platform.redis.msg.entity;

import com.cluster.platform.redis.msg.producer.ProducerUtil;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Advance
 * @date 2022年03月21日 14:31
 * @since V1.0.0
 */
public class MessageBody<V> implements Serializable {

    /**
     * 序列号
     */
    private static final long serialVersionUID = -3489122590773686296L;

    /**
     * 操作类型
     */
    private Operation oprType;

    /**
     * UUID
     */
    private String uuid;

    /**
     * 数据
     */
    private Collection<V> data;

    public MessageBody(Operation oprType, Collection<V> data) {
        this.oprType = oprType;
        this.data = data;
        this.uuid = ProducerUtil.uuid;
    }

    /**
     * 获取 操作类型
     *
     * @return 操作类型.
     */
    public Operation getOprType() {
        return oprType;
    }

    /**
     * 设置 操作类型
     *
     * @param oprType 操作类型
     */
    public void setOprType(Operation oprType) {
        this.oprType = oprType;
    }

    /**
     * 获取 数据
     *
     * @return 数据.
     */
    public Collection<V> getData() {
        return data;
    }

    /**
     * 设置 数据
     *
     * @param data 数据
     */
    public void setData(Collection<V> data) {
        this.data = data;
    }

    /**
     * 设置 uuid
     *
     * @param uuid uuid
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * 获取 uuid
     *
     * @return uuid.
     */
    public String getUuid() {
        return uuid;
    }

    public enum Operation {
        /**
         * Redis读操作
         */
        R,
        /**
         * Redis写操作
         */
        W,
        /**
         * Redis删除操作
         */
        D;
    }
}
