package com.hao.amqp;

import com.hao.amqp.bean.Book;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class SpringbootAmqpApplicationTests {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    AmqpAdmin amqpAdmin;

    @Test
    void contextLoads() {
		//Message需要自己构造一个;定义消息体内容和消息头
		//rabbitTemplate.send(exchage,routeKey,message);

		//object默认当成消息体，只需要传入要发送的对象，自动序列化发送给rabbitmq；
		//rabbitTemplate.convertAndSend(exchage,routeKey,object);
		Map<String,Object> map = new HashMap<>();
		map.put("msg","这是第一个消息");
		map.put("data", Arrays.asList("helloworld",123,true));
		//对象被默认序列化以后发送出去
		rabbitTemplate.convertAndSend("exchange.direct","atguigu.news",new Book("西游记","吴承恩"));
    }

	//1、单播（点对点）
	@Test
	public void pointToPoint() {
		Map<String, Object> map = new HashMap<>();
		map.put("msg", "这是第一个消息");
		map.put("data", Arrays.asList("helloworld", 123, true));
		rabbitTemplate.convertAndSend("exchange.direct", "haohj", map);
	}

	//2、广播
	@Test
	public void sendMsg() {
		rabbitTemplate.convertAndSend("exchange.fanout", "", new Book("吴承恩", "西游记"));
	}

	//接受数据，如何将数据自动转为json发送出去
	@Test
	public void receive() {
		Object o = rabbitTemplate.receiveAndConvert("haohj");
		System.out.println(o.getClass());
		System.out.println(o);
	}

	@Test
	public void creatExchanges() {
		//创建direct类型的交换机
		DirectExchange directExchange = new DirectExchange("exchange.direct");
		amqpAdmin.declareExchange(directExchange);

		//创建fanout类型的交换机
		FanoutExchange fanoutExchange = new FanoutExchange("exchange.fanout");
		amqpAdmin.declareExchange(fanoutExchange);

		//创建topic类型的交换机
		TopicExchange topicExchange = new TopicExchange("exchange.topic");
		amqpAdmin.declareExchange(topicExchange);
	}

	@Test
	public void createQueues() {
		Queue queue = new Queue("haohj");
		amqpAdmin.declareQueue(queue);

		Queue queue2 = new Queue("haohj.news");
		amqpAdmin.declareQueue(queue2);

		Queue queue3 = new Queue("haohj.emps");
		amqpAdmin.declareQueue(queue3);

		Queue queue4 = new Queue("haoxue.news");
		amqpAdmin.declareQueue(queue4);
	}

	@Test
	public void createBindings() {
		//direct交换机绑定所有queue
		Binding binding = new Binding("haohj", Binding.DestinationType.QUEUE, "exchange.direct", "haohj", null);
		amqpAdmin.declareBinding(binding);
		binding = new Binding("haohj.emps", Binding.DestinationType.QUEUE, "exchange.direct", "haohj.emps", null);
		amqpAdmin.declareBinding(binding);
		binding = new Binding("haohj.news", Binding.DestinationType.QUEUE, "exchange.direct", "haohj.news", null);
		amqpAdmin.declareBinding(binding);
		binding = new Binding("haoxue.news", Binding.DestinationType.QUEUE, "exchange.direct", "haoxue.news", null);
		amqpAdmin.declareBinding(binding);

		//fanout交换机绑定所有queue
		binding = new Binding("haohj", Binding.DestinationType.QUEUE, "exchange.fanout", "haohj", null);
		amqpAdmin.declareBinding(binding);
		binding = new Binding("haohj.emps", Binding.DestinationType.QUEUE, "exchange.fanout", "haohj.emps", null);
		amqpAdmin.declareBinding(binding);
		binding = new Binding("haohj.news", Binding.DestinationType.QUEUE, "exchange.fanout", "haohj.news", null);
		amqpAdmin.declareBinding(binding);
		binding = new Binding("haoxue.news", Binding.DestinationType.QUEUE, "exchange.fanout", "haoxue.news", null);
		amqpAdmin.declareBinding(binding);

		//topic交换机绑定所有queue
		binding = new Binding("haohj", Binding.DestinationType.QUEUE, "exchange.topic", "haohj.#", null);
		amqpAdmin.declareBinding(binding);
		binding = new Binding("haohj.emps", Binding.DestinationType.QUEUE, "exchange.topic", "haohj.#", null);
		amqpAdmin.declareBinding(binding);
		binding = new Binding("haohj.news", Binding.DestinationType.QUEUE, "exchange.topic", "*.news", null);
		amqpAdmin.declareBinding(binding);
		binding = new Binding("haoxue.news", Binding.DestinationType.QUEUE, "exchange.topic", "*.news", null);
		amqpAdmin.declareBinding(binding);
	}

	@Test
	public void getQueue(){
		QueueInformation queueInfo = amqpAdmin.getQueueInfo("haohj.news");
		System.out.println(queueInfo.getName());
	}
}
