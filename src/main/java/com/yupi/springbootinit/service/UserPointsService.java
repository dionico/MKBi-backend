package com.yupi.springbootinit.service;

import com.yupi.springbootinit.model.entity.UserPoints;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author DIO
* @description 针对表【user_points(用户积分记录)】的数据库操作Service
* @createDate 2024-04-05 16:31:35
*/
public interface UserPointsService extends IService<UserPoints> {
    /**
     * 根据 当前用户ID 获取积分总数
     * @param userId
     * @return
     */
    Integer getTotalPoints(Long userId);

    /**
     * 获取可用积分数
     */
    Integer getCurrentPoints(Long userId);

    /**
     * 每日签到
     * @param userId
     * @return
     */
    Boolean signUser(Long userId);

    /**
     * 更新积分（内部方法） 正数为增加积分，负数为消耗积分
     * @param userId
     * @param points
     * @return
     */
    Boolean updatePoints(Long userId,long points);

}
