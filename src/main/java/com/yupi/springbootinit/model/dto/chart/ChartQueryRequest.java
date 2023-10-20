package com.yupi.springbootinit.model.dto.chart;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author <a href="https://magicalmirai.com">Mikufans</a>
 * @from <a href="https://www.tw-pjsekai.com/">世界计划，缤纷舞台</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChartQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * 名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;
    /**
     * 图标类型
     */
    private String chartType;
    /**
     * 用户id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}