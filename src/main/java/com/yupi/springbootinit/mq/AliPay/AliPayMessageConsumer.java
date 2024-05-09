package com.yupi.springbootinit.mq.AliPay;

import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.AliPayMqConstant;
import com.yupi.springbootinit.constant.OrdersConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.entity.RechargeOrders;
import com.yupi.springbootinit.service.RechargeOrdersService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 阿里支付队列消费者
 * 消费正常队列中的消息
 */
@Component
@Slf4j
public class AliPayMessageConsumer {


    @Resource
    RechargeOrdersService ordersService;

    /**
     * 处理接收到的消息，检查消息是否为空，为空则拒绝消息并抛出异常
     * 解析消息为订单ID，查询订单信息
     * 根据订单的 tradeStatus 更新订单状态或重新放回队列
     * @param message
     * @param channel
     * @param deliveryTag
     */
    @SneakyThrows
    @RabbitListener(queues = {AliPayMqConstant.ORDERS_QUEUE_NAME})//监听 MqConstant.ORDERS_QUEUE_NAME 队列中的消息
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        log.warn("接收到队列信息，receiveMessage={}=======================================",message);
        if (StringUtils.isBlank(message)){
            //消息为空，消息拒绝，不重复发送，不重新放入队列
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息为空");
        }
        long orderId = Long.parseLong(message);
        RechargeOrders order = ordersService.getById(orderId);
        if (order == null){
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"订单为空");
        }
        //查询订单信息看是否完成支付，未支付则重新放入队列中直至过期
        String tradeStatus = String.valueOf(order.getOrderStatus());
        log.warn("订单查询为"+order.getOrderStatus());
        if (!tradeStatus.equals(OrdersConstant.SUCCEED)){
            log.warn("订单未支付成功,重新放回队列,订单号为"+order.getId());
            channel.basicNack(deliveryTag,false,true);
        }else {
            //消息确认
            channel.basicAck(deliveryTag,false);
        }
    }
}
