package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 充值订单表
 * @TableName recharge_orders
 */
@TableName(value ="recharge_orders")
@Data
public class RechargeOrders implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 用户ID，关联user表id
     */
    private Long userId;

    /**
     * 充值金额
     */
    private BigDecimal amount;

    /**
     * 获得积分数量
     */
    private Integer pointsReceived;

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