package com.yupi.springbootinit.model.dto.points;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Mikufans
 * @title UserPointsAddRequest
 * @description <更新请求>
 * @from <a href="https://www.tw-pjsekai.com/">世界计划，缤纷舞台</a>
 */
@Data
public class UserPointsUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 创建用户Id
     */
    private Long userId;

    /**
     * 总积分
     */
    private Integer totalPoints;

    /**
     * 当前可用积分（扣除已使用、过期等后的积分）
     */
    private Integer currentPoints;

    /**
     * 积分来源：充值、购买、活动奖励等
     */
    private String pointSource;

    /**
     * 关联的充值订单ID（如充值来源时填写）
     */
    private Long rechargeOrderId;



    private static final long serialVersionUID = 1L;

}
