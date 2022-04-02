package com.cluster.platform.redis.msg.producer;

import com.platform.core.util.IDGenerator;
import com.platform.core.util.SpringContextUtil;
import com.cluster.platform.redis.msg.entity.MessageBody.Operation;

/**
 * @author Advance
 * @date 2022年03月21日 14:32
 * @since V1.0.0
 */
public class ProducerUtil {
    /**
     * UUID
     */
    public static String uuid = IDGenerator.generate(64);

    /**
     * 消息生成者
     */
    private IProducer producer;

    protected ProducerUtil() {

    }

    /**
     * 初始化生产者
     *
     * @see
     * @since 1.0
     */
    private void init() {
        if (this.producer == null) {
            this.producer = SpringContextUtil.getBean(Producer.class);
        }
    }

    /**
     * produce the message
     *
     * @param opr operation
     * @param obj message body
     * @see
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public <V> void produce(Operation opr, V... obj) {
        init();
        if (producer != null) {
            producer.produce(opr, obj);
        }
    }

    /**
     * produce the message
     *
     * @param channel topic channel
     * @param opr operation
     * @param obj message body
     * @see
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public <V> void produce(String channel, Operation opr, V... obj) {
        init();
        if (producer != null) {
            producer.produce(channel, opr, obj);
        }
    }
}
