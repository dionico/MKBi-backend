package com.yupi.springbootinit.model.entity;

import lombok.Data;

/**
 * @author Mikufans
 * @title UserWithPoints
 * @description <设置用户登录态>
 * @from <a href="https://www.tw-pjsekai.com/">世界计划，缤纷舞台</a>
 */
@Data
public class UserWithPoints {
    /**
     * 用户信息
     */
    private User user;

    /**
     * 积分
     */
    private Integer totalPoints;
}
