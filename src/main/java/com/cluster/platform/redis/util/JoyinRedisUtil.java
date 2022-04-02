package com.cluster.platform.redis.util;

import com.cluster.platform.redis.dto.SelectOptionDto;
import com.platform.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Advance
 * @date 2022年03月21日 14:18
 * @since V1.0.0
 */
public class JoyinRedisUtil {
    private static Logger logger = LoggerFactory.getLogger(JoyinRedisUtil.class);

    /**
     * 私有构造
     */
    private JoyinRedisUtil() {
        // Private to this construct
    }

    /**
     * 获取map中第一个非空数据值
     *
     * @param <K> Key的类型
     * @param <V> Value的类型
     * @param map 数据源
     * @return 返回的值
     */
    public static <K, V> V getFirstNotNull(Map<K, V> map) {
        V obj = null;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            obj = entry.getValue();
            if (obj != null) {
                break;
            }
        }
        return obj;
    }

    /**
     * 将数组转化为列表
     *
     * @param arr 数组
     * @return 列表
     */
    public static <V> List<V> arrayToList(V[] arr) {
        List<V> list = new ArrayList<>();
        for (V v : arr) {
            list.add(v);
        }
        return list;
    }

    public static <V> void addItem(Map<String, V> targetMap, String key, V info) {
        targetMap.computeIfAbsent(key, k -> targetMap.put(k, info));
    }

    public static final void copyToByteArr(byte[] a, String str, int start) {
        byte[] temp = str.getBytes();
        copyToByteArr(temp, a, start);
    }

    public static final void copyToByteArr(byte[] dest, byte[] src, int start) {
        System.arraycopy(src, 0, dest, start, src.length);
    }

    public static final void copyToByteArr(byte[] dest, byte[] src, int start, int length) {
        System.arraycopy(src, start, dest, 0, length);
    }

    /*public static final SelectOptionDto createSltOpDto(BaseDictCodeList dictCodeList) {
        SelectOptionDto dto = new SelectOptionDto();
        dto.setId(dictCodeList.getValue());
        dto.setText(dictCodeList.getCodeName());
        return dto;
    }*/

    /**
     * DB字段名称转换 ITEM_TYPE->itemType
     *
     * @param dbCol DB字段名称
     * @return 转换结果
     * @see
     * @since 1.0
     */
    public static final String dbColToFiledName(String dbCol) {
        if (StringUtil.isEmpty(dbCol)) {
            return dbCol;
        }
        StringBuilder builder = new StringBuilder();
        String[] arr = dbCol.toLowerCase().split("_");
        builder.append(arr[0]);
        if (arr.length > 1) {
            for (int i = 1; i < arr.length; i++ ) {
                if (StringUtil.isEmpty(arr[i])) {
                    continue;
                }
                builder.append(arr[i].substring(0, 1).toUpperCase());
                if (arr[i].length() > 1) {
                    builder.append(arr[i].substring(1));
                }
            }
        }
        return builder.toString();
    }

    /*public static final boolean isType(String type, BaseDictCodeList dict) {
        return StringUtil.equals(type, dict.getValue());
    }*/

    public static final <V, O> void put(ThreadLocal<Map<V, O>> local, V v, O o) {
        Map<V, O> map = getMap(local);
        map.put(v, o);
    }

    public static final <V, O> Map<V, O> getMap(ThreadLocal<Map<V, O>> local) {
        Map<V, O> map = local.get();
        if (map == null) {
            map = new HashMap<>();
            local.set(map);
        }
        return map;
    }

    public static final <V, O> O getMapVal(ThreadLocal<Map<V, O>> local, V v) {
        Map<V, O> map = getMap(local);
        return map.get(v);
    }

    public static final String max(String str1, String str2) {
        if (StringUtil.isEmpty(str1)) {
            return str2;
        }
        if (StringUtil.isEmpty(str2)) {
            return str1;
        }
        return str1.compareTo(str2) > 0 ? str1 : str2;
    }

    /**
     * 取得 Hash的值
     *
     * @param map Map(K,H,V)
     * @param k K
     * @param h H
     * @return V
     * @see
     * @since 1.0
     */
    public static <K, H, V> V getValue(Map<K, Map<H, V>> map, K k, H h) {
        Map<H, V> temp = map == null ? null : map.get(k);
        return temp == null ? null : temp.get(h);
    }

    public static final String min(String str1, String str2) {
        if (StringUtil.isEmpty(str1)) {
            return str1;
        }
        if (StringUtil.isEmpty(str2)) {
            return str2;
        }
        return str1.compareTo(str2) > 0 ? str2 : str1;
    }

    @SuppressWarnings("unchecked")
    public static final <T> T getFieldValue(Object obj, String field) {
        Class<?> clz = obj.getClass();
        try {
            Field fd = getField(clz, field);
            return fd == null ? null : (T)fd.get(obj);
        }
        catch (SecurityException e) {
            logger.error("NoSuchFieldException | SecurityException : {}", field);
        }
        catch (IllegalArgumentException | IllegalAccessException e) {
            logger.error("IllegalArgumentException | IllegalAccessException : {}", field);
        }
        return null;
    }

    public static final Field getField(Class<?> clz, String field) {
        try {
            Field fd = clz.getDeclaredField(field);
            fd.setAccessible(true);
            return fd;
        }
        catch (NoSuchFieldException | SecurityException e) {
            Class<?> superClass = clz.getSuperclass();
            if (superClass != null) {
                return getField(superClass, field);
            }
            else {
                logger.error("NoSuchFieldException | SecurityException : {}", field);
            }
        }
        return null;
    }
}
