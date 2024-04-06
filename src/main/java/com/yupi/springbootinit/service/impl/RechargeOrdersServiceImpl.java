package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.model.entity.RechargeOrders;
import com.yupi.springbootinit.service.RechargeOrdersService;
import com.yupi.springbootinit.mapper.RechargeOrdersMapper;
import org.springframework.stereotype.Service;

/**
* @author DIO
* @description 针对表【recharge_orders(充值订单表)】的数据库操作Service实现
* @createDate 2024-04-05 16:20:43
*/
@Service
public class RechargeOrdersServiceImpl extends ServiceImpl<RechargeOrdersMapper, RechargeOrders>
    implements RechargeOrdersService{

}




