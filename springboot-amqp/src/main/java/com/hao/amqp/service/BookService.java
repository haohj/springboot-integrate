package com.hao.amqp.service;

import com.hao.amqp.bean.Book;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class BookService {

//    @RabbitListener(queues = "haohj.news")
    public void receive(Book book) {
        System.out.println("收到消息：" + book);
    }

//    @RabbitListener(queues = "haohj")
    public void receive02(String message, Channel channel, Message msg) {
        try{
            System.out.println("收到haohj消息  : " + message);
            //rankService.executeRank(message);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
