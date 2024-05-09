package com.yupi.springbootinit.mq.AliPay;

import com.yupi.springbootinit.constant.AliPayMqConstant;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 声明并初始化消息队列相关实体（队列、交换机、绑定）
 * 用于创建订单程序的交换机和队列
 */
@Configuration
public class AliPayMqInitMain {

    /**
     * QueueBuilder 创建一个持久化的队列（重启后仍存在）
     */
    @Bean
    Queue AliPayDeadQueue(){
        return QueueBuilder.durable(AliPayMqConstant.ORDERS_DEAD_QUEUE_NAME).build();
    }

    /**
     * 声明一个直连交换机（Direct Exchange），消息会根据路由键直接投递到与之匹配的队列。
     * @return
     */
    @Bean
    DirectExchange AliPayDeadExchange() {
        return new DirectExchange(AliPayMqConstant.ORDERS_DEAD_EXCHANGE_NAME);
    }


    /**
     * 将死信队列与死信交换机进行绑定，指定路由键为 MqConstant.ORDERS_DEAD_ROUTING_KEY
     * @param AliPayDeadQueue
     * @param AliPayDeadExchange
     * @return
     */
    @Bean
    Binding AliPayDeadBinding(Queue AliPayDeadQueue, DirectExchange AliPayDeadExchange) {
        return BindingBuilder.bind(AliPayDeadQueue).to(AliPayDeadExchange).with(AliPayMqConstant.ORDERS_DEAD_ROUTING_KEY);
    }

    /**
     * 将队列和交换机声明
     */
    @Bean
    Queue AliPayQueue(){
        Map<String,Object> arg = new HashMap<>();
        //信息参数 设置消息存活时间（TTL）为1分钟，超时消息将被自动移除并投递到死信交换机
        arg.put("x-message-ttl",1000*60);
        //绑定死信队列（当消息超时或无法投递时，消息将转发至此交换机）
        arg.put("x-dead-letter-exchange",AliPayMqConstant.ORDERS_DEAD_EXCHANGE_NAME);
        //死信消息的路由键
        arg.put("x-dead-letter-routing-key",AliPayMqConstant.ORDERS_DEAD_ROUTING_KEY);
        //创建持久化队列并设置属性
        return QueueBuilder.durable(AliPayMqConstant.ORDERS_QUEUE_NAME).withArguments(arg).build();
    }

    /**
     * 声明一个名为 MqConstant.ORDERS_EXCHANGE_NAME 的直连交换机，用于处理正常消息
     * @return
     */
    @Bean
    DirectExchange AliPayExchange() {
        return new DirectExchange(AliPayMqConstant.ORDERS_EXCHANGE_NAME);
    }

    /**
     * 将正常队列与直连交换机进行绑定，指定路由键为 MqConstant.ORDERS_ROUTING_KEY
     * @param AliPayQueue
     * @param AliPayExchange
     * @return
     */
    @Bean
    Binding AliPayBinding(Queue AliPayQueue, DirectExchange AliPayExchange) {
        return BindingBuilder.bind(AliPayQueue).to(AliPayExchange).with(AliPayMqConstant.ORDERS_ROUTING_KEY);
    }


}
