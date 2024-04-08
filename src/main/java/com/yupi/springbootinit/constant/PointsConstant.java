package com.yupi.springbootinit.constant;

/**
 * 积分表常量
 */
public interface PointsConstant {

    /**
     * 初始积分
     */
    Integer INITIAL_POINTS = 10;

    /**
     * 签到积分
     */
    Integer CREDIT_DAILY = 5;

    /**
     * 生成图表消耗积分
     */
    Integer CREDIT_CHART_SUCCESS = -1;

    /**
     * 生成图表失败返回积分
     */
    Integer CREDIT_CHART_FALSE = 1;

    /**
     * 积分来源
     */
    String DAILY = "签到";

    String PAY = "充值";

    String REGISTER = "注册";

}
