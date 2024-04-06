package com.yupi.springbootinit.constant;

/**
 * 积分表常量
 */
public interface PointsConstant {

    /**
     * 签到积分
     */
    long CREDIT_DAILY = 5;

    /**
     * 生成图表消耗积分
     */
    long CREDIT_CHART_SUCCESS = -1;

    /**
     * 生成图表失败返回积分
     */
    long CREDIT_CHART_FALSE = 1;

    /**
     * 积分来源
     */
    String DAILY = "签到";

    String PAY = "充值";

}
