package com.yupi.springbootinit.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.PointsConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.RedisLimiterManager;
import com.yupi.springbootinit.model.entity.UserPoints;
import com.yupi.springbootinit.service.UserPointsService;
import com.yupi.springbootinit.mapper.UserPointsMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

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
//    @Override
//    public Integer getCurrentPoints(Long userId) {
//        return fetchUserPoints(userId).getCurrentPoints();
//    }

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
        if (userId == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        synchronized (userId.toString().intern()) {
            // 先查询用户积分记录是否存在，不存在则创建
            QueryWrapper<UserPoints> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userId", userId);
            UserPoints userPoints = this.getOne(queryWrapper);
            // 如果用户积分记录不存在，创建一条新的记录
            if (userPoints == null) {
                userPoints = new UserPoints();
                userPoints.setUserId(userId);
                userPoints.setTotalPoints(PointsConstant.INITIAL_POINTS); // 分配初始积分
                userPoints.setPointSource(PointsConstant.DAILY);
                this.save(userPoints);
            }
            ThrowUtils.throwIf(userPoints == null, ErrorCode.NOT_FOUND_ERROR);

            // 判断今天是否已经签到
            LocalDate today = LocalDate.now(ZoneId.systemDefault());
            // 检查lastSignInDate是否为空
            if (userPoints.getLastSignInDate() != null){
                // 将已有的签到日期转换为LocalDate进行比较
                LocalDate lastSignInDate = userPoints.getLastSignInDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                ThrowUtils.throwIf(today.equals(lastSignInDate),ErrorCode.TOO_MANY_REQUEST);
                return false;
            }
            // 更新积分并设置新的签到日期
            Integer userTotalPoints = Math.toIntExact(userPoints.getTotalPoints() + PointsConstant.CREDIT_DAILY);
            userPoints.setTotalPoints(userTotalPoints);
            userPoints.setLastSignInDate(new Date());

            return this.updateById(userPoints);
        }
    }

    /**
     * 更新积分
     * @param userId
     * @param points
     * @return
     */
    @Override
    public Boolean updatePoints(Long userId, Integer points) {
        if (userId == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        QueryWrapper<UserPoints> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        UserPoints userPoints = this.getOne(queryWrapper);
        ThrowUtils.throwIf(userPoints == null, ErrorCode.NOT_FOUND_ERROR);
        Integer userTotalPoints = userPoints.getTotalPoints();
        //积分不足时
        if (userTotalPoints+points<0) return false;
        userTotalPoints = Math.toIntExact(userTotalPoints + points);
        userPoints.setTotalPoints(userTotalPoints);
        //保持更新时间
        userPoints.setUpdateTime(null);
        return this.updateById(userPoints);
    }

    /**
     * 更新积分（主要是增加加积分时调用）
     * @param userId
     * @param points
     * @param source
     * @return
     */
    @Override
    public Boolean updatePoints(Long userId, Integer points, String source) {
        if (userId == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        QueryWrapper<UserPoints> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        UserPoints userPoints = this.getOne(queryWrapper);
        ThrowUtils.throwIf(userPoints == null, ErrorCode.NOT_FOUND_ERROR);
        Integer userTotalPoints = userPoints.getTotalPoints();
        //积分不足时
        if (userTotalPoints+points<0) return false;
        userTotalPoints = Math.toIntExact(userTotalPoints + points);
        userPoints.setTotalPoints(userTotalPoints);
        userPoints.setPointSource(source);
        //保持更新时间
        userPoints.setUpdateTime(null);
        return this.updateById(userPoints);
    }


}




