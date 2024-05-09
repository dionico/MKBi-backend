package com.yupi.springbootinit.constant;

/**
 * 订单支付消息队列
 */
public interface AliPayMqConstant {
    /**
     * 订单支付队列常量
     */
    String ORDERS_EXCHANGE_NAME = "orders_exchange";

    String ORDERS_QUEUE_NAME="orders_queue";

    String ORDERS_ROUTING_KEY="orders_routingKey";
    /**
     * 死信队列常量
     */
    String ORDERS_DEAD_EXCHANGE_NAME="orders_dead_exchange";
    String ORDERS_DEAD_QUEUE_NAME="orders_dead_queue";

    String ORDERS_DEAD_ROUTING_KEY="orders_dead_routingKey";
}
