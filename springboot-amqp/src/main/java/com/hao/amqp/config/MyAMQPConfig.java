package com.hao.amqp.config;

import org.apache.commons.lang3.BooleanUtils;
import com.rabbitmq.client.DefaultSaslConfig;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class MyAMQPConfig {
    /*@Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }*/

    public static final String DEFAULT_BOOK_QUEUE = "dev.book.register.default.queue";
    public static final String MANUAL_BOOK_QUEUE = "dev.book.register.manual.queue";

    @Bean
    public Queue defaultBookQueue() {
        // 第一个是 QUEUE 的名字,第二个是消息是否需要持久化处理
        return new Queue(DEFAULT_BOOK_QUEUE, true);
    }

    @Bean
    public Queue manualBookQueue() {
        // 第一个是 QUEUE 的名字,第二个是消息是否需要持久化处理
        return new Queue(MANUAL_BOOK_QUEUE, true);
    }

    @Autowired
    RabbitProperties rabbitProperties;
    @Autowired
    CachingConnectionFactory cachingConnectionFactory;

    /**
     * 解决安全扫描 AMQP明文登录漏洞 仅当rabbitmq启用ssl时并且配置证书时，显式设置EXTERNAL认证机制<br/>
     * EXTERNAL认证机制使用X509认证方式，服务端读取客户端证书中的CN作为登录名称，同时忽略密码
     */
    @PostConstruct
    public void rabbitmqSslExternalPostConstruct() {
        boolean rabbitSslEnabled = BooleanUtils.toBoolean(rabbitProperties.getSsl().getEnabled());
        boolean rabbitSslKeyStoreExists = rabbitProperties.getSsl().getKeyStore() != null;
        if (rabbitSslEnabled && rabbitSslKeyStoreExists) {
            cachingConnectionFactory.getRabbitConnectionFactory().setSaslConfig(DefaultSaslConfig.EXTERNAL);
        }
    }
}
