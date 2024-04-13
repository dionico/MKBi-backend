package com.yupi.springbootinit.model.dto.points;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 查询请求
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PointsQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 创建用户Id
     */
    private Long userId;

    /**
     * 总积分数量
     */
    private Integer totalPoints;

    /**
     * 积分来源：充值、购买、活动奖励等
     */
    private String pointSource;

    /**
     * 关联的充值订单ID（如充值来源时填写）
     */
    private Long rechargeOrderId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    private static final long serialVersionUID = 1L;
}
