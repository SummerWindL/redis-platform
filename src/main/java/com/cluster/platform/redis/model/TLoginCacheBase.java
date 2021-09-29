package com.cluster.platform.redis.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author: Advance
 * @date: 2018/4/26
 * @description:
 */
@Setter
@Getter
@ToString
public class TLoginCacheBase implements Serializable {

    private static final long serialVersionUID = -1243128610306063718L;

    private String oslang;

    private String token;

}
