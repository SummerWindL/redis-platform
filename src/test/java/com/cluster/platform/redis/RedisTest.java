package com.cluster.platform.redis;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @program: platform-base
 * @description:
 * @author: fuyl
 * @create: 2020-06-05 10:01
 **/
public class RedisTest extends PlatformRedisApplicationTest {
    @Autowired
    private ICache cache;
    @Test
    public void test(){
        cache.putHashValue("mytable:r6","{'222222':'5656'}","{'898239','12323212'}");
        System.out.println("插入成功");
    }
}
