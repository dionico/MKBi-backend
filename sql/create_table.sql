# 数据库初始化

-- 创建库
create database if not exists MKbi;

-- 切换库
use MKbi;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    gender       tinyint      default 0                 not null comment '性别：0女1男',
    phone        varchar(32)                            null comment '电话',
    birth        datetime                               null comment '出生日期',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_userAccount (userAccount)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 图表信息表
create table if not exists chart
(
    id         bigint auto_increment comment 'id' primary key,
    goal       text  null  comment '分析目标',
    `name`     varchar(128) null  comment '图表名称',
    status     ENUM('wait', 'running', 'succeed', 'failed') not null comment 'wait,running,succeed,failed',
    execMessage text null comment '执行信息',
    chartData  text  null comment '图表数据',
    chartType  varchar(128) null comment '图标类型',
    genChart    text  null comment '生成的图表数据',
    genResult  text  null comment  '生成的分析结论',
    userId     bigint null comment '创建者id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '图表信息表' collate = utf8mb4_unicode_ci;

-- 用户积分表
create table if not exists user_points
(
    id              bigint auto_increment comment '主键' primary key,
    userId          bigint                        not null comment '用户ID，关联user表id',
    totalPoints     int                           not null default 0 comment '总积分数量',
    currentPoints   int                           not null default 0 comment '当前可用积分（扣除已使用、过期等后的积分）',
    pointSource     varchar(64)                  not null comment '积分来源：充值、购买、活动奖励等',
    rechargeOrderId bigint                         null comment '关联的充值订单ID（如充值来源时填写）',
    createTime      datetime     default CURRENT_TIMESTAMP not null comment '积分获取时间',
    updateTime      datetime     default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint      default 0                 not null comment '是否删除'
) comment '用户积分记录' collate = utf8mb4_unicode_ci;


-- 充值订单表
create table if not exists recharge_orders
(
    id              bigint auto_increment comment '主键' primary key,
    orderId         varchar(64)                  not null comment '订单号',
    userId          bigint                        not null comment '用户ID，关联user表id',
    amount          decimal(10, 2)               not null comment '充值金额',
    pointsReceived  int                           not null comment '获得积分数量',
    paymentMethod   varchar(64)                  not null comment '支付方式：例如支付宝沙盒',
    transactionId   varchar(128)                 null comment '支付宝沙盒交易ID',
    orderStatus     enum('pending', 'paying', 'succeed', 'failed', 'refunded') not null default 'pending' comment '订单状态',
    createTime      datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    payTime         datetime                            null comment '支付成功时间',
    updateTime      datetime     default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    refundReason    varchar(256)                 null comment '退款原因（若退款）',
    isDelete        tinyint      default 0                 not null comment '是否删除'
) comment '充值订单表' collate = utf8mb4_unicode_ci;