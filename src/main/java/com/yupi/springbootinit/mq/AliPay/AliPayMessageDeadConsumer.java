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
 * 阿里支付队列的死信队列
 * 消费死信队列中的消息
 */
@Component
@Slf4j
public class AliPayMessageDeadConsumer {

    @Resource
    private RechargeOrdersService ordersService;


    /**
     * 处理接收到的消息，检查消息是否为空，为空则拒绝消息并抛出异常
     * 解析消息为订单ID，查询订单信息
     * 修改订单状态为失败，并确认消息
     * @param message
     * @param channel
     * @param deliveryTag
     */
    @SneakyThrows
    @RabbitListener(queues = {AliPayMqConstant.ORDERS_DEAD_QUEUE_NAME})
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        log.warn("接收到死信队列信息，receiveMessage={}=======================================",message);
        if (StringUtils.isBlank(message)){
            //消息为空，消息拒绝，不重复发送，不重新放入队列
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息为空");
        }
        long ordersId = Long.parseLong(message);
        RechargeOrders orders = ordersService.getById(ordersId);
        if (orders == null){
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"订单为空");
        }

        //修改表状态为执行中，执行成功修改为“已完成”；执行失败修改为“失败”
        RechargeOrders updateOrders = new RechargeOrders();
        updateOrders.setId(orders.getId());
        updateOrders.setOrderStatus(OrdersConstant.FAILED);
        boolean updateResult = ordersService.updateById(updateOrders);
        if (!updateResult){
            handleOrdersUpdateError(orders.getId(),"更新订单执行状态失败");
            return;
        }
        //消息确认
        channel.basicAck(deliveryTag,false);
    }
    private void handleOrdersUpdateError(Long ordersId, String execMessage) {
        RechargeOrders updateOrdersResult = new RechargeOrders();
        updateOrdersResult.setOrderStatus(OrdersConstant.FAILED);
        updateOrdersResult.setId(ordersId);
        boolean updateResult = ordersService.updateById(updateOrdersResult);
        if (!updateResult){
            log.error("更新订单失败状态失败"+ordersId+","+execMessage);
        }
    }
}
