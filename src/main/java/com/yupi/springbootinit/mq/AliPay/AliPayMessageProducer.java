package com.yupi.springbootinit.mq.AliPay;

import com.yupi.springbootinit.constant.AliPayMqConstant;
import com.yupi.springbootinit.constant.BiMqConstant;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 发送消息到消息队列
 */
@Component
public class AliPayMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     * @param message
     */
    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(AliPayMqConstant.ORDERS_EXCHANGE_NAME, AliPayMqConstant.ORDERS_ROUTING_KEY, message);
    }

}
