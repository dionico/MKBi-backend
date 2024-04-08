package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户积分记录
 * @TableName user_points
 */
@TableName(value ="user_points")
@Data
public class UserPoints implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联user表id
     */
    private Long userId;

    /**
     * 总积分数量
     */
    private Integer totalPoints;

//    /**
//     * 当前可用积分（扣除已使用、过期等后的积分）
//     */
    private Integer currentPoints;

    /**
     * 积分来源：充值、购买、活动奖励等
     */
    private String pointSource;

    /**
     * 关联的充值订单ID（如充值来源时填写）
     */
    private Long rechargeOrderId;

    /**
     * 积分获取时间
     */
    private Date createTime;

    /**
     * 签到时间
     */
    private Date lastSignInDate;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}