package com.cluster.platform.redis.core;

import com.cluster.platform.redis.data.IRedisHash;
import com.platform.common.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Advance
 * @date 2022年03月21日 15:31
 * @since V1.0.0
 */
@Component
public class PipeReadService<K, F, V> extends PipeBaseService<K, F, V> {

    /**
     * 批量取得Redis数据
     *
     * @param vList 条件
     * @return Redis数据
     * @see
     * @since 1.0
     */
    public Map<K, Map<F, V>> readList(Map<K, Map<F, V>> vList) {
        return pipeReadForHash(vList);
    }

    /**
     * 读取Redis数据
     *
     * @param key 条件
     * @return Redis数据
     * @see
     * @since 1.0
     */
    public V read(IRedisHash<K, F, V> key) {
        Map<K, Map<F, V>> resMap = readList(changeToMap(key));
        if (resMap != null) {
            K k = key.getKey();
            Map<F, V> m = resMap.get(k);
            if (m != null) {
                F f = key.getField();
                return m.get(f);
            }
        }
        return null;
    }

    /**
     * 取得 指定Key的所有hash数据<b>比较耗时，性能差</b>
     *
     * @param keyList Key
     * @return hash数据
     * @see
     * @since 1.0
     */
    public Map<K, Map<F, V>> readAll(List<K> keyList) {
        if (StringUtil.isEmptyList(keyList)) {
            return new HashMap<>();
        }
        Map<K, Map<F, V>> paramList = new HashMap<>();
        keyList.forEach(key -> {
            Map<F, V> entity = new HashMap<>();
            paramList.put(key, entity);
        });
        return pipeReadAllForHash(paramList);
    }

    /**
     * 取得redis集群所有的keys
     *
     * @param k 匹配元素
     * @return redis集群所有的keys
     * @see
     * @since 1.0
     */
    public List<String> allKeys(String k) {
        return keys(k);
    }

    /**
     * 取得redis集群所有的keys
     *
     * @param k 匹配元素
     * @return redis集群所有的keys
     * @see
     * @since 1.0
     */
    public List<String> allHKeys(String k, String field) {
        return hKeys(k, field);
    }
}
