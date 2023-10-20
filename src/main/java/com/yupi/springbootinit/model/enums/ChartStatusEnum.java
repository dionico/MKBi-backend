package com.yupi.springbootinit.model.enums;

/**
 * 图表运行状态
 * @author <a href="https://magicalmirai.com">Mikufans</a>
 * @from <a href="https://www.tw-pjsekai.com/">世界计划，缤纷舞台</a>
 */
public enum ChartStatusEnum {
    WAIT("wait"),
    RUNNING("running"),
    SUCCEED("succeed"),
    FAILED("failed");

    private String value;

    ChartStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
