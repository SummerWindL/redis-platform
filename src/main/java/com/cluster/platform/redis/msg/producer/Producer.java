package com.cluster.platform.redis.msg.producer;

import com.cluster.platform.redis.msg.entity.MessageBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author Advance
 * @date 2022年03月21日 14:45
 * @since V1.0.0
 */
@Service
class Producer implements IProducer {

    /**
     * Jedis
     */
    @Autowired
    private RedisTemplate<?, ?> redisTemplate;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <V> void produce(String channel, MessageBody.Operation opr, V... v) {
        List<V> vSet = Arrays.asList(v);
        MessageBody<V> obj = new MessageBody<>(opr, vSet);
        redisTemplate.convertAndSend(channel, obj);
    }
}
