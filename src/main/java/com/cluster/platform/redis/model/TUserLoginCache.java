package com.cluster.platform.redis.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.sf.json.JSONArray;

import java.util.List;

@ToString
@Setter
@Getter
public class TUserLoginCache extends TLoginCacheBase {

	private static final long serialVersionUID = 8489186590376672642L;

	/**
	 * 消息推送主题
	 */
	private String mqtopic;

	private String acc;

	private List<String> hospList;

	private JSONArray menu;

	private JSONArray hospDeptArray;

}