package com.yupi.springbootinit.constant;

/**
 * 消息队列常量
 */
public interface BiMqConstant {

    /**
     * 交换机
     */
    String BI_EXCHANGE_NAME = "bi_exchange";

    /**
     * 队列名
     */
    String BI_QUEUE_NAME = "bi_queue";

    /**
     * 绑定键
     */
    String BI_ROUTING_KEY = "bi_routingKey";

    /**
     * 死信队列常量
     */
    String BI_DEAD_EXCHANGE_NAME="bi_dead_exchange";

    String BI_DEAD_QUEUE_NAME="bi_dead_queue";

    String BI_DEAD_ROUTING_KEY="bi_dead_routingKey";
}
