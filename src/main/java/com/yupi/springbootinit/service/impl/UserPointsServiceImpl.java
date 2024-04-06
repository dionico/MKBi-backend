package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.entity.UserPoints;
import com.yupi.springbootinit.service.UserPointsService;
import com.yupi.springbootinit.mapper.UserPointsMapper;
import org.springframework.stereotype.Service;

/**
* @author DIO
* @description 针对表【user_points(用户积分记录)】的数据库操作Service实现
* @createDate 2024-04-05 16:31:35
*/
@Service
public class UserPointsServiceImpl extends ServiceImpl<UserPointsMapper, UserPoints>
    implements UserPointsService{

    /**
     * 根据 当前用户ID 获取积分总数
     * @param userId
     * @return
     */
    @Override
    public Integer getTotalPoints(Long userId) {
        return fetchUserPoints(userId).getTotalPoints();
    }

    /**
     * 根据 当前用户ID 获取可用积分
     * @param userId
     * @return
     */
    @Override
    public Integer getCurrentPoints(Long userId) {
        return fetchUserPoints(userId).getCurrentPoints();
    }

    /**
     * 获取积分
     * @param userId
     * @return
     */
    private UserPoints fetchUserPoints(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        QueryWrapper<UserPoints> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        UserPoints userPoints = this.getOne(queryWrapper);
        ThrowUtils.throwIf(userPoints == null, ErrorCode.NOT_FOUND_ERROR);
        return userPoints;
    }

    /**
     * 每日签到
     * @param userId
     * @return
     */
    @Override
    public Boolean signUser(Long userId) {
        return null;
    }

    /**
     * 更新积分
     * @param userId
     * @param points
     * @return
     */
    @Override
    public Boolean updatePoints(Long userId, long points) {
        return null;
    }


}




