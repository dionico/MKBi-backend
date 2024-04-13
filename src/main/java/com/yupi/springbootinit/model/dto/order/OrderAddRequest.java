package com.yupi.springbootinit.model.dto.order;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 创建充值订单表
 * @TableName orders
 */
@Data
public class OrderAddRequest implements Serializable {

    /**
     * 交易名称
     */
    private String subject;

    /**
     * 交易金额
     */
    private BigDecimal totalAmount;

    /**
     * 支付方式：例如支付宝沙盒
     */
    private String paymentMethod;

    private static final long serialVersionUID = 1L;
}
