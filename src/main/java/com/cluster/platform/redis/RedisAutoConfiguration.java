package com.cluster.platform.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Arrays;

@Configuration
//@ConfigurationProperties(prefix = "platform.aix.redis")
public class RedisAutoConfiguration {

    /**
     * 集群配置服务器
     * @author Advance
     * @date 2022/1/19 20:12
     * @param null
     * @return null
     */
//    private String cluster = "101.35.80.154:7001," +
//            "101.35.80.154:7002," +
//            "101.35.80.154:7003," +
//            "101.35.80.154:7004," +
//            "101.35.80.154:7005," +
//            "101.35.80.154:7006";

    @Value("${platform.aix.redis.useCluster: false}")
    private boolean useCluster;

    @Value("${platform.aix.redis.cluster: 101.35.80.154:7001,101.35.80.154:7002,101.35.80.154:7003,101.35.80.154:7004,101.35.80.154:7005,101.35.80.154:7006}")
    private String cluster;

	@Bean
	@ConditionalOnMissingBean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        template.setConnectionFactory(factory);
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        return template;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
	    //是否启用集群
	    if(useCluster){
            // 开启自适应集群拓扑刷新和周期拓扑刷新，不开启相应槽位主节点挂掉会出现服务不可用，直到挂掉节点重新恢复
            ClusterTopologyRefreshOptions clusterTopologyRefreshOptions =  ClusterTopologyRefreshOptions.builder()
                    .enableAllAdaptiveRefreshTriggers() // 开启自适应刷新,自适应刷新不开启,Redis集群变更时将会导致连接异常
                    .adaptiveRefreshTriggersTimeout(Duration.ofSeconds(30)) //自适应刷新超时时间(默认30秒)，默认关闭开启后时间为30秒
                    .enablePeriodicRefresh(Duration.ofSeconds(20))  // 默认关闭开启后时间为60秒 ClusterTopologyRefreshOptions.DEFAULT_REFRESH_PERIOD 60  .enablePeriodicRefresh(Duration.ofSeconds(2)) = .enablePeriodicRefresh().refreshPeriod(Duration.ofSeconds(2))
                    .build();
            ClientOptions clientOptions = ClusterClientOptions.builder()
                    .topologyRefreshOptions(clusterTopologyRefreshOptions)
                    .build();
            // 客户端读写分离配置
            LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                    .clientOptions(clientOptions)
                    .build();
            RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(Arrays.asList(
                    cluster.split(",")));
            new LettuceConnectionFactory(redisClusterConfiguration, clientConfig);
        }
        return new JedisConnectionFactory();
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
}
