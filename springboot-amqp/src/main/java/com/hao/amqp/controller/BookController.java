package com.hao.amqp.controller;

import com.hao.amqp.bean.Book;
import com.hao.amqp.config.MyAMQPConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/books")
public class BookController {
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public BookController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * this.rabbitTemplate.convertAndSend(RabbitConfig.DEFAULT_BOOK_QUEUE, book); 对应 {@link BookHandler#listenerAutoAck}
     * this.rabbitTemplate.convertAndSend(RabbitConfig.MANUAL_BOOK_QUEUE, book); 对应 {@link BookHandler#listenerManualAck}
     */
    @GetMapping
    public void defaultMessage() {
        Book book = new Book();
        book.setAuthor("1");
        book.setBookName("一起来学Spring Boot");
        this.rabbitTemplate.convertAndSend(MyAMQPConfig.DEFAULT_BOOK_QUEUE, book);
        this.rabbitTemplate.convertAndSend(MyAMQPConfig.MANUAL_BOOK_QUEUE, book);
    }
}
