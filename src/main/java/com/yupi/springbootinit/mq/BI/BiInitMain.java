package com.yupi.springbootinit.mq.BI;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.yupi.springbootinit.constant.BiMqConstant;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于创建测试程序用到的交换机和队列（只用在程序启动前执行一次）
 */
@Configuration
public class BiInitMain {
    /**
     * 将死信队列和交换机声明
     */
    @Bean
    Queue BiDeadQueue(){
        return QueueBuilder.durable(BiMqConstant.BI_DEAD_QUEUE_NAME).build();
    }

    @Bean
    DirectExchange BiDeadExchange() {
        return new DirectExchange(BiMqConstant.BI_DEAD_EXCHANGE_NAME);
    }


    @Bean
    Binding BiDeadBinding(Queue BiDeadQueue, DirectExchange BiDeadExchange) {
        return BindingBuilder.bind(BiDeadQueue).to(BiDeadExchange).with(BiMqConstant.BI_DEAD_ROUTING_KEY);
    }

    /**
     * 将队列和交换机声明
     */
    @Bean
    Queue BiQueue(){
        //信息参数 设置TTL为1min
        Map<String,Object> arg = new HashMap<>();
        arg.put("x-message-ttl",60000);
        //绑定死信交换机
        arg.put("x-dead-letter-exchange",BiMqConstant.BI_DEAD_EXCHANGE_NAME);
        arg.put("x-dead-letter-routing-key",BiMqConstant.BI_DEAD_ROUTING_KEY);
        return QueueBuilder.durable(BiMqConstant.BI_QUEUE_NAME).withArguments(arg).build();
    }

    @Bean
    DirectExchange BiExchange() {
        return new DirectExchange(BiMqConstant.BI_EXCHANGE_NAME);
    }

    @Bean
    Binding BiBinding(Queue BiQueue, DirectExchange BiExchange) {
        return BindingBuilder.bind(BiQueue).to(BiExchange).with(BiMqConstant.BI_ROUTING_KEY);
    }

    public static void main(String[] args) {
        try {
            //创建链接工厂
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            //建立链接
            Connection connection = factory.newConnection();
            //创建频道
            Channel channel = connection.createChannel();
            //声明交换机
            String EXCHANGE_NAME =  BiMqConstant.BI_EXCHANGE_NAME;
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            // 创建队列
            String queueName = BiMqConstant.BI_QUEUE_NAME;
            channel.queueDeclare(queueName, true, false, false, null);
            //绑定交换机和队列
            channel.queueBind(queueName, EXCHANGE_NAME,  BiMqConstant.BI_ROUTING_KEY);
        } catch (Exception e) {

        }
    }
}
