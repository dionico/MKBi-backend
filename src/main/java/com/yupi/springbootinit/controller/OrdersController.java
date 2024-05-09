package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.dto.order.OrdersQueryRequest;
import com.yupi.springbootinit.model.entity.RechargeOrders;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.service.RechargeOrdersService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单接口
 *
 */
@RestController
@RequestMapping("/userOrders")
@Slf4j
public class OrdersController {


    @Resource
    private RechargeOrdersService rechargeOrdersService;

    @Resource
    private UserService userService;


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
        RechargeOrders oldUserPoints = rechargeOrdersService.getById(id);
        ThrowUtils.throwIf(oldUserPoints == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldUserPoints.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = rechargeOrdersService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<RechargeOrders> getUserPointsById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        RechargeOrders orders = rechargeOrdersService.getById(id);
        if (orders == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(orders);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param ordersQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<RechargeOrders>> listUserPointsByPage(@RequestBody OrdersQueryRequest ordersQueryRequest,
            HttpServletRequest request) {
        long current = ordersQueryRequest.getCurrent();
        long size = ordersQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<RechargeOrders> userOrdersPage = rechargeOrdersService.page(new Page<>(current, size),
                getQueryWrapper(ordersQueryRequest));
        return ResultUtils.success(userOrdersPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param ordersQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<RechargeOrders>> listMyUserPointsByPage(@RequestBody OrdersQueryRequest ordersQueryRequest,
            HttpServletRequest request) {
        if (ordersQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        ordersQueryRequest.setUserId(loginUser.getId());
        long current = ordersQueryRequest.getCurrent();
        long size = ordersQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<RechargeOrders> userOrdersPage = rechargeOrdersService.page(new Page<>(current, size),
                getQueryWrapper(ordersQueryRequest));
        return ResultUtils.success(userOrdersPage);
    }

    // endregion


    /**
     * 根据传入的PointsQueryRequest参数构建一个查询条件封装对象，
     * 以便于后续在数据库中执行更具针对性的查询操作
     * @param ordersQueryRequest
     * @return
     */
    private QueryWrapper<RechargeOrders> getQueryWrapper(OrdersQueryRequest ordersQueryRequest) {
        // QueryWrapper是MyBatis-Plus库提供的用于构建动态SQL查询语句的工具。
        QueryWrapper<RechargeOrders> queryWrapper = new QueryWrapper<>();

        if (ordersQueryRequest == null) {
            return queryWrapper;
        }

        Long orderId = ordersQueryRequest.getId();
        Object orderStatus = ordersQueryRequest.getOrderStatus();
        BigDecimal amount = ordersQueryRequest.getAmount();
        String subject = ordersQueryRequest.getSubject();
        String paymentMethod = ordersQueryRequest.getPaymentMethod();
        String transactionId = ordersQueryRequest.getTransactionId();
        Date createTime = ordersQueryRequest.getCreateTime();
        Date payTime = ordersQueryRequest.getPayTime();
        Date updateTime = ordersQueryRequest.getUpdateTime();
        Long userId = ordersQueryRequest.getUserId();
        String sortField = ordersQueryRequest.getSortField();
        String sortOrder = ordersQueryRequest.getSortOrder();

        queryWrapper.eq(orderId != null && orderId > 0, "id", orderId);
        queryWrapper.eq(orderStatus != null, "orderStatus", orderStatus);
        queryWrapper.eq(amount != null, "amount", amount);
        queryWrapper.eq(StringUtils.isNotBlank(subject), "subject", subject);
        queryWrapper.eq(StringUtils.isNotBlank(paymentMethod), "paymentMethod", paymentMethod);
        queryWrapper.eq(StringUtils.isNotBlank(transactionId), "transactionId", transactionId);
        queryWrapper.eq(payTime != null, "payTime", payTime);
        queryWrapper.le(updateTime != null, "updateTime", updateTime);
        queryWrapper.le(createTime != null, "createTime", createTime);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.eq("isDelete", false);

        // 根据传入的排序字段和排序方式对查询结果进行排序
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);

        return queryWrapper;
    }

}
