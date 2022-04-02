package com.cluster.platform.redis.core;

import com.cluster.platform.redis.data.RedisHash;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author Advance
 * @date 2022年03月21日 15:31
 * @since V1.0.0
 */
@Component
public class RedisHashOperator<K, H, V> extends PipeBaseService<K, H, V> {

    public RedisHash<K, H, V> read(K key, H field) {
        Map<K, Map<H, V>> param = new HashMap<>();
        Map<H, V> map = new HashMap<>();
        map.put(field, null);
        param.put(key, map);
        RedisHash<K, H, V> entity = new RedisHash<>();
        entity.setKey(key);
        entity.setField(field);
        Map<K, Map<H, V>> resMap = pipeReadForHash(param);
        if (resMap != null && !resMap.isEmpty()) {
            Map<H, V> value = resMap.get(key);
            if (value != null && !value.isEmpty()) {
                V v = value.get(field);
                entity.setValue(v);
            }
        }
        return entity;
    }

    public List<RedisHash<K, H, V>> readList(Map<K, Collection<H>> paramMap) {
        if (paramMap == null || paramMap.isEmpty()) {
            return new ArrayList<>();
        }
        Map<K, Map<H, V>> paramList = new HashMap<>();
        paramMap.entrySet().forEach(en -> {
            K k = en.getKey();
            Collection<H> hList = en.getValue();
            if (CollectionUtils.isEmpty(hList)) {
                return;
            }
            Map<H, V> hash = new HashMap<>();
            hList.forEach(h -> hash.put(h, null));
            paramList.put(k, hash);
        });
        Map<K, Map<H, V>> resMap = pipeReadForHash(paramList);
        return mapToEntity(resMap);
    }

    public void write(RedisHash<K, H, V> entity) {
        pipeWriteForHash(changeToMap(entity));
    }

    private List<RedisHash<K, H, V>> mapToEntity(Map<K, Map<H, V>> resMap) {
        List<RedisHash<K, H, V>> resList = new ArrayList<>();
        if (resMap != null && !resMap.isEmpty()) {
            resMap.entrySet().forEach(mapEn -> {
                K k = mapEn.getKey();
                Map<H, V> vmap = mapEn.getValue();
                if (vmap != null && !vmap.isEmpty()) {
                    vmap.entrySet().forEach(en -> {
                        RedisHash<K, H, V> temp = new RedisHash<>();
                        temp.setKey(k);
                        temp.setField(en.getKey());
                        temp.setValue(en.getValue());
                        resList.add(temp);
                    });
                }
            });
        }
        return resList;
    }
}
