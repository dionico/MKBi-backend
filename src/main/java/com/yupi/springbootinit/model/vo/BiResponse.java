package com.yupi.springbootinit.model.vo;

import lombok.Data;

/**
 * Bi 的返回结果
 */
@Data
public class BiResponse {
    /**
     * 图表代码
     */
    private String genChart;
    /**
     * 结论
     */
    private String genResult;
    /**
     * 图表id
     */
    private Long chartId;
}
