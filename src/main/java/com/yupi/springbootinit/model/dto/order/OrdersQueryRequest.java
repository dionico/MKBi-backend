package com.yupi.springbootinit.model.dto.order;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 查询请求
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OrdersQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
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




    private static final long serialVersionUID = 1L;
}
