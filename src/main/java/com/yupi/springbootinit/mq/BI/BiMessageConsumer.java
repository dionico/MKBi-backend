package com.yupi.springbootinit.mq.BI;

import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.BiMqConstant;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.manager.SparkManager;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.enums.ChartStatusEnum;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.utils.ExcelUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

import static com.yupi.springbootinit.constant.CommonConstant.PROMPT2;
import static javax.swing.text.html.HTML.Attribute.PROMPT;

/**
 * 消息消费者
 *
 */
@Slf4j
@Component
public class BiMessageConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;
    @Resource
    private SparkManager sparkManager;

    //指定程序监听的消息队列和确认机制
    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(value = AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        if (StringUtils.isBlank(message)){
            //消息拒绝
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        //message就是生产者传过来的图表Id
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if (chart == null){
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图表为空");
        }
        // 先修改图表任务状态为 “执行中”。
        // 等执行成功后，修改为 “已完成”、保存执行结果；
        // 执行失败后，状态修改为 “失败”，记录任务失败信息。
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus(ChartStatusEnum.RUNNING.getValue());
        boolean b = chartService.updateById(updateChart);
        if (!b) {
            channel.basicNack(deliveryTag, false, false);
            handleChartUpdateError(chart.getId(), "更新 '图表执行中' 状态失败");
            return;
        }
        //调用AI
//        String result = aiManager.doChat(CommonConstant.BI_MODEL_ID, buildUserInput(chart));
        //调用讯飞星火AI
        String result = sparkManager.sendMesToAIUseXingHuo( PROMPT2 + buildUserInput(chart));
//        String[] splits = result.split("【【【【【");
        String[] splits = result.split("@@@@@");
        if (splits.length < 3) {
            channel.basicNack(deliveryTag, false, false);
            //chart.setStatus(ChartStatusEnum.FAILED.getValue());

            // 更新图表状态为失败
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setStatus(ChartStatusEnum.FAILED.getValue()); // 设置状态为失败
            boolean updateResult = chartService.updateById(updateChartResult);

            if (!updateResult) {
                // 如果更新失败，记录错误信息
                handleChartUpdateError(chart.getId(), "更新 '图表生成失败' 状态失败");
            }

//            handleChartUpdateError(chart.getId(), "图表生成失败");
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
            channel.basicNack(deliveryTag, false, false);
            handleChartUpdateError(chart.getId(), "更新 '图表生成成功' 状态失败");
        }

        log.info("receiveMessage message = {} deliveryTag = {}", message, deliveryTag);
        try {
            //确认消息
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            //todo 消息失败放入到死信队列中
            log.error("消息确认失败 message={}", e.getMessage());
        }
    }

    /**
     * 构造用户输入
     * @param chart
     * @return
     */
    private String buildUserInput(Chart chart){
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();

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
        userInput.append(csvData).append("\n");
        return userInput.toString();
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
}