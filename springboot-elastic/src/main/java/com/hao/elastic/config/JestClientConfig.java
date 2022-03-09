package com.hao.elastic.config;

import com.google.gson.GsonBuilder;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JestClientConfig {
    @Bean
    public JestClient getJestCline() {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder("http://192.2.2.124:9200")
//                .Builder("http://192.168.140.128:9200")
                .gson(new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss").create())
                .multiThreaded(true)
                .build());
        return factory.getObject();
    }


}
