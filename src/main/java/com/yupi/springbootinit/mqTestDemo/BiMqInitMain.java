package com.yupi.springbootinit.mqTestDemo;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 创建测试程序用到的交换机和队列
 */
public class BiMqInitMain {

    public static void main(String[] argv) throws Exception {

        try {
            //创建链接工厂
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            //建立链接、创建频道
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            //交换机名
            String exchangeName = "test_exchange";
            channel.exchangeDeclare(exchangeName, "direct");
            //队列名
            String queueName = "test_queue";
            channel.queueDeclare(queueName, true, false, false, null);
            //绑定队列和交换机
            channel.queueBind(queueName, exchangeName, "test_routing_key");
        } catch (Exception e){

        }
    }
}