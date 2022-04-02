package com.cluster.platform.redis.util;

import com.cluster.platform.redis.msg.producer.ProducerUtil;

/**
 * @author Advance
 * @date 2022年03月21日 14:30
 * @since V1.0.0
 */
public class ProducerUtils extends ProducerUtil {

    /**
     * Instance of ProducerUtils
     */
    private static ProducerUtils instance;

    private ProducerUtils() {
        // 私有化构造
    }

    /**
     * get the Instance of ProducerUtils
     *
     * @return Instance of ProducerUtils
     * @see
     * @since 1.0
     */
    public static final ProducerUtils getInstance() {
        if (instance == null) {
            instance = new ProducerUtils();
        }
        return instance;
    }
}
