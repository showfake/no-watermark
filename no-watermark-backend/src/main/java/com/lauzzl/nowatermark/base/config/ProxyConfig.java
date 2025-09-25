package com.lauzzl.nowatermark.base.config;

import com.dtflys.forest.http.ForestProxy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "forest.proxy")
public class ProxyConfig {

    private Boolean enable;

    private String type;

    private String host;

    private Integer port;

    private String username;

    private String password;

    @Bean
    public ForestProxy proxy() {
        if (!enable) {
            return null;
        }
        if (type.equals("socks")) {
            return ForestProxy.socks(host, port).username(username).password(password);
        }
        if (type.equals("http")) {
            return ForestProxy.http(host, port).username(username).password(password);
        }
        throw new IllegalArgumentException("proxy model error");
    }


}
