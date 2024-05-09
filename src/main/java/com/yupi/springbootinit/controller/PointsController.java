package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.constant.PointsConstant;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.dto.points.PointsQueryRequest;
import com.yupi.springbootinit.model.dto.points.UserPointsAddRequest;
import com.yupi.springbootinit.model.dto.points.UserPointsUpdateRequest;
import com.yupi.springbootinit.model.entity.UserPoints;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.service.UserPointsService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.SqlUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 积分接口
 *
 */
@RestController
@RequestMapping("/userPoints")
@Slf4j
public class PointsController {


    @Resource
    private UserPointsService userPointsService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    public BaseResponse<Long> addUserPoints(@RequestBody UserPointsAddRequest userPointsAddRequest, HttpServletRequest request) {
        if (userPointsAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserPoints userPoints = new UserPoints();
        BeanUtils.copyProperties(userPointsAddRequest, userPoints);

        Long userId = userPointsAddRequest.getUserId();
        userPoints.setUserId(userId);

        //报存积分记录到积分表
        boolean result = userPointsService.save(userPoints);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR,"更新用户积分表失败");
        //获取新添加的积分记录id
        long newUserPointsId = userPoints.getId();

        //更新用户表中的积分字段
        User user = userService.getById(userId);//获取用户实体
        user.setTotalPoints(userPoints.getTotalPoints());//设置新积分值
        boolean userUpdateResult = userService.updateById(user);//更新用户表
        ThrowUtils.throwIf(!userUpdateResult, ErrorCode.OPERATION_ERROR,"更新用户表积分失败");
        return ResultUtils.success(newUserPointsId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUserPoints(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserPoints oldUserPoints = userPointsService.getById(id);
        ThrowUtils.throwIf(oldUserPoints == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldUserPoints.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = userPointsService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param userPointsUpdateRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUserPoints(@RequestBody UserPointsUpdateRequest userPointsUpdateRequest) {
        if (userPointsUpdateRequest == null || userPointsUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserPoints userPoints = new UserPoints();
        BeanUtils.copyProperties(userPointsUpdateRequest, userPoints);
        long id = userPointsUpdateRequest.getId();
        // 判断是否存在
        UserPoints oldUserPoints = userPointsService.getById(id);
        ThrowUtils.throwIf(oldUserPoints == null, ErrorCode.NOT_FOUND_ERROR);

        Long userId = userPointsUpdateRequest.getUserId();//获取对应用户id
        User user = userService.getById(userId);
        user.setTotalPoints(userPoints.getTotalPoints());//更新用户表的积分

        boolean result = userPointsService.updateById(userPoints);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<UserPoints> getUserPointsById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserPoints userPoints = userPointsService.getById(id);
        if (userPoints == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(userPoints);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param PointsQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserPoints>> listUserPointsByPage(@RequestBody PointsQueryRequest PointsQueryRequest,
            HttpServletRequest request) {
        long current = PointsQueryRequest.getCurrent();
        long size = PointsQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<UserPoints> userPointsPage = userPointsService.page(new Page<>(current, size),
                getQueryWrapper(PointsQueryRequest));
        return ResultUtils.success(userPointsPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param PointsQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<UserPoints>> listMyUserPointsByPage(@RequestBody PointsQueryRequest PointsQueryRequest,
            HttpServletRequest request) {
        if (PointsQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        PointsQueryRequest.setUserId(loginUser.getId());
        long current = PointsQueryRequest.getCurrent();
        long size = PointsQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<UserPoints> userPointsPage = userPointsService.page(new Page<>(current, size),
                getQueryWrapper(PointsQueryRequest));
        return ResultUtils.success(userPointsPage);
    }

    // endregion

    /**
     * 每日签到
     * @param request
     * @return
     */
    @GetMapping("/sign")
    public BaseResponse<Boolean> signUserPoints(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        Boolean result = userPointsService.signUser(userId);
        ThrowUtils.throwIf(result, ErrorCode.OPERATION_ERROR, "签到失败");

        Integer userPoints = userPointsService.getTotalPoints(userId);
        // 通过 UserService 更新用户表中的积分
        userService.updateUserPoints(userId, userPoints);

        return ResultUtils.success(result);
    }


    /**
     * 根据传入的PointsQueryRequest参数构建一个查询条件封装对象，
     * 以便于后续在数据库中执行更具针对性的查询操作
     * @param PointsQueryRequest
     * @return
     */
    private QueryWrapper<UserPoints> getQueryWrapper(PointsQueryRequest PointsQueryRequest) {
        // QueryWrapper是MyBatis-Plus库提供的用于构建动态SQL查询语句的工具。
        QueryWrapper<UserPoints> queryWrapper = new QueryWrapper<>();

        if (PointsQueryRequest == null) {
            return queryWrapper;
        }

        Integer totalPoints = PointsQueryRequest.getTotalPoints();
//        Integer currentPoints = PointsQueryRequest.getCurrentPoints();
        String pointSource = PointsQueryRequest.getPointSource();
        Long rechargeOrderId = PointsQueryRequest.getRechargeOrderId();
        Date updateTime = PointsQueryRequest.getUpdateTime();
        Date createTime = PointsQueryRequest.getCreateTime();
        String sortField = PointsQueryRequest.getSortField();
        String sortOrder = PointsQueryRequest.getSortOrder();
        Long id = PointsQueryRequest.getId();
        Long userId = PointsQueryRequest.getUserId();


        queryWrapper.eq(id!=null &&id>0,"id",id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(totalPoints),"totalPoints",totalPoints);
//        queryWrapper.eq(ObjectUtils.isNotEmpty(currentPoints),"currentPoints",currentPoints);
        queryWrapper.eq(ObjectUtils.isNotEmpty(pointSource),"pointSource",pointSource);
        queryWrapper.eq(ObjectUtils.isNotEmpty(rechargeOrderId),"rechargeOrderId",rechargeOrderId);

        //小于等于查询时间
        queryWrapper.le(ObjectUtils.isNotEmpty(updateTime),"updateTime",updateTime);
        queryWrapper.le(ObjectUtils.isNotEmpty(createTime),"createTime",createTime);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId),"userId",userId);
        queryWrapper.eq("isDelete", false);
        //根据传入的排序字段和排序方式对查询结果进行排序
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

}
