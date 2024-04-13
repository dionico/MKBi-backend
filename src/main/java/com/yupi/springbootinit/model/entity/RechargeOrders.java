package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 充值订单表
 * @TableName recharge_orders
 */
@TableName(value ="recharge_orders")
@Data
public class RechargeOrders implements Serializable {
    /**
     * 订单id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联user表id
     */
    private Long userId;

    /**
     * 交易名称
     */
    private String subject;

    /**
     * 支付宝买家id
     */
    private String buyerId;

    /**
     * 充值金额
     */
    private BigDecimal amount;

    /**
     * 支付方式：例如支付宝沙盒
     */
    private String paymentMethod;

    /**
     * 支付宝沙盒交易ID
     */
    private String transactionId;

    /**
     * 订单状态
     */
    private Object orderStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 支付成功时间
     */
    private Date payTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 退款原因（若退款）
     */
    private String refundReason;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}