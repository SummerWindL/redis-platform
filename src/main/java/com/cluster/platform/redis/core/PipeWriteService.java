package com.cluster.platform.redis.core;

import com.cluster.platform.redis.data.IRedisHash;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: Advance
 * @create: 2022-03-21 15:31
 * @since V1.0.0
 */
@Component
public class PipeWriteService<K, F, V> extends PipeBaseService<K, F, V> {

    /**
     * 加载指定数据到Redis
     *
     * @param infoList 指定数据列表
     * @see
     * @since 1.0
     */
    public void writeList(Map<K, Map<F, V>> infoList) {
        pipeWriteForHash(infoList);
    }

    /**
     * 删除部分数据
     *
     * @param infoList 数据列表
     * @see
     * @since 1.0
     */
    public void deleteList(Map<K, Map<F, V>> infoList) {
        pipeDelForHash(infoList);
    }

    /**
     * 删除单个数据
     *
     * @param info 数据
     * @see
     * @since 1.0
     */
    public void deleteOne(Map<K, Map<F, V>> info) {
        deleteList(info);
    }

    /**
     * 删除所有数据
     *
     * @param info 数据主键信息
     * @see
     * @since 1.0
     */
    public void deleteAll(IRedisHash<K, F, V> info) {
        pipeDelAllForHash(changeToMap(info));
    }

    /**
     * 删除所有Redis数据
     *
     * @see
     * @since 1.0
     */
    public void flushDB() {
        flushAll();
    }
}
