package com.hao.consumer;

import com.hao.consumer.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringbootDubboConsumerUserApplicationTests {
    @Autowired
    UserService userService;

    @Test
    void contextLoads() {
    	userService.hello();
    }

}
