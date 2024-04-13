package com.yupi.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
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
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.manager.RedisLimiterManager;
import com.yupi.springbootinit.manager.SparkManager;
import com.yupi.springbootinit.model.dto.chart.*;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.enums.ChartStatusEnum;
import com.yupi.springbootinit.model.vo.BiResponse;
import com.yupi.springbootinit.mq.BiMessageProducer;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.service.UserPointsService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.ExcelUtils;
import com.yupi.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.yupi.springbootinit.constant.CommonConstant.PRECONDITION;
import static com.yupi.springbootinit.constant.CommonConstant.PROMPT;

/**
 * 图表接口
 *
 * @author <a href="https://magicalmirai.com">Mikufans</a>
 * @from <a href="https://www.tw-pjsekai.com/">世界计划，缤纷舞台</a>
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BiMessageProducer biMessageProducer;

    @Resource
    private SparkManager sparkManager;

    @Resource
    private UserPointsService userPointsService;


    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                        HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                          HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion
    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }


    /**
     * 智能分析（同步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                             GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        //校验
        //如果目标为空，抛出错误提示
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"目标为空");
        //如果名字不为空或者名字过长
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100,ErrorCode.PARAMS_ERROR,"名称过长");
        //校验文件
        long size = multipartFile.getSize();//获取文件大小
        String originalFilename = multipartFile.getOriginalFilename();//获取文件后缀名
        //校验文件大小
        final long  ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件大小超过 1M");
        //校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");


        //当前用户信息
        User loginUser = userService.getLoginUser(request);
        //限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

        //模型id
        long biModelId = CommonConstant.BI_MODEL_ID;
        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        // 分析需求：
        // 分析网站用户的增长情况
        // 原始数据：
        // 日期,用户数
        // 1号,10
        // 2号,20
        // 3号,30
        //系统预设，提前告诉AI他的职责、功能、回复格式要求
        userInput.append("分析需求：").append("\n");

        //拼接分析目标
        String userGoal = goal;
        //如果用户选择了图表类型
        if (StringUtils.isNotBlank(chartType)){
            userGoal += ". 请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        //压缩后的数据(将Excel转换成CSV)
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        //调用鱼聪明AI
//        String result = aiManager.doChat(biModelId,userInput.toString());
        //判断积分是否足够
        ThrowUtils.throwIf(userPointsService.updatePoints(loginUser.getId(), PointsConstant.CREDIT_CHART_SUCCESS), ErrorCode.OPERATION_ERROR, "积分不足");
        //调用讯飞星火AI
        String result = sparkManager.sendMesToAIUseXingHuo( PROMPT + userInput.toString());
        String[] splits = result.split("【【【【【");
        if (splits.length < 3){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI 生成错误");
        }
        String genChart = splits[1].trim();//图表代码
        String genResult = splits[2].trim();//分析结论
        //插入到数据库
        Chart chart = new Chart();
        //请求名称
        chart.setName(name);
        //分析目标
        chart.setGoal(goal);
        //原始数据
        chart.setChartData(csvData);
        //图表类型
        chart.setChartType(chartType);
        //生成的图表代码
        chart.setGenChart(genChart);
        //生成的结论
        chart.setGenResult(genResult);
        //运行状态
        chart.setStatus(ChartStatusEnum.SUCCEED.getValue());
        //  用户id
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"图表保存失败");
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);

    }


    /**
     * 智能分析（异步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        //校验
        //如果目标为空，抛出错误提示
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        //如果名字不为空或者名字过长
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        //校验文件
        long size = multipartFile.getSize();//获取文件大小
        String originalFilename = multipartFile.getOriginalFilename();//获取文件后缀名
        //校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件大小超过 1M");
        //校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");


        //当前用户信息
        User loginUser = userService.getLoginUser(request);
        //限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

        //模型id
        long biModelId = CommonConstant.BI_MODEL_ID;
        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        //拼接分析目标
        String userGoal = goal;
        //如果用户选择了图表类型
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ". 请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        //压缩后的数据(将Excel转换成CSV)
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        //插入到数据库
        Chart chart = new Chart();
        //请求名称
        chart.setName(name);
        //分析目标
        chart.setGoal(goal);
        //原始数据
        chart.setChartData(csvData);
        //图表类型
        chart.setChartType(chartType);
        //运行状态
        chart.setStatus(ChartStatusEnum.WAIT.getValue());
        //  用户id
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        CompletableFuture.runAsync(() -> {
            // 先修改图表任务状态为 “执行中”。
            // 等执行成功后，修改为 “已完成”、保存执行结果；
            // 执行失败后，状态修改为 “失败”，记录任务失败信息。
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus(ChartStatusEnum.RUNNING.getValue());
            boolean b = chartService.updateById(updateChart);
            if (!b) {
                handleChartUpdateError(chart.getId(), "更新 '图表执行中' 状态失败");
                return;
            }
            //调用AI
//            String result = aiManager.doChat(biModelId, userInput.toString());
            //判断积分是否足够
            ThrowUtils.throwIf(userPointsService.updatePoints(loginUser.getId(), PointsConstant.CREDIT_CHART_SUCCESS), ErrorCode.OPERATION_ERROR, "积分不足");
            //调用讯飞星火AI
            String result = sparkManager.sendMesToAIUseXingHuo( PROMPT + userInput.toString());
            String[] splits = result.split("【【【【【");
            if (splits.length < 3) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
            }
            String genChart = splits[1].trim();//图表代码
            String genResult = splits[2].trim();//分析结论
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);
            updateChartResult.setStatus(ChartStatusEnum.SUCCEED.getValue());
            boolean updateResult = chartService.updateById(updateChartResult);
            if (!updateResult) {
                handleChartUpdateError(chart.getId(), "更新 '图表生成成功' 状态失败");
            }
        }, threadPoolExecutor);
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }


    /**
     * 智能分析（异步消息队列 ）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<BiResponse> genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        //校验
        //如果目标为空，抛出错误提示
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        //如果名字不为空或者名字过长
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        //校验文件
        long size = multipartFile.getSize();//获取文件大小
        String originalFilename = multipartFile.getOriginalFilename();//获取文件后缀名
        //校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件大小超过 1M");
        //校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
        //当前用户信息
        User loginUser = userService.getLoginUser(request);
        //限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
        //模型id
        long biModelId = CommonConstant.BI_MODEL_ID;
        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        //拼接分析目标
        String userGoal = goal;
        //如果用户选择了图表类型
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ". 请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        //压缩后的数据(将Excel转换成CSV)
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        //插入到数据库
        Chart chart = new Chart();
        //请求名称
        chart.setName(name);
        //分析目标
        chart.setGoal(goal);
        //原始数据
        chart.setChartData(csvData);
        //图表类型
        chart.setChartType(chartType);
        //运行状态
        chart.setStatus(ChartStatusEnum.WAIT.getValue());
        //  用户id
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        long newChartId= chart.getId();
        //判断积分是否足够
        ThrowUtils.throwIf(userPointsService.updatePoints(loginUser.getId(), PointsConstant.CREDIT_CHART_SUCCESS), ErrorCode.OPERATION_ERROR, "积分不足");
        //生产者发送图表Id给消费者，具体的事务处理在消费者中进行
        biMessageProducer.sendMessage(String.valueOf(newChartId));
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(newChartId);
        return ResultUtils.success(biResponse);
    }


    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus(ChartStatusEnum.FAILED.getValue());
        updateChartResult.setExecMessage("execMessage");
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            log.error("更新 '图表生成失败' 状态失败" + chartId + "," + execMessage);
        }
    }

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        queryWrapper.eq(id != null && id > 0,"id", id);
        queryWrapper.like(StringUtils.isNotBlank(name),"name", name);
        queryWrapper.eq(StringUtils.isNotBlank(goal),"chartType", chartType);
        queryWrapper.eq(StringUtils.isNotBlank(goal),"goal", goal);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

}
