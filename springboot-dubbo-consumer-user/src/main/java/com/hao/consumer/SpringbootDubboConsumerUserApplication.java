package com.hao.consumer;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 1、引入依赖
 * 2、配置dubbo的注册中心地址
 * 3、引用服务
 */
@EnableDubbo
@SpringBootApplication
public class SpringbootDubboConsumerUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootDubboConsumerUserApplication.class, args);
    }

}
