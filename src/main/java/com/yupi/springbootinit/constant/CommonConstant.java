package com.yupi.springbootinit.constant;

/**
 * 通用常量
 *
 * @author <a href="https://magicalmirai.com">Mikufans</a>
 * @from <a href="https://www.tw-pjsekai.com/">世界计划，缤纷舞台</a>
 */
public interface CommonConstant {

    /**
     * 升序
     */
    String SORT_ORDER_ASC = "ascend";

    /**
     * 降序
     */
    String SORT_ORDER_DESC = " descend";

    /**
     * AI 模型id
     */
    long BI_MODEL_ID = 1659171950288818178L;

    /**
     * AI生成问题的预设条件
     */
    String PRECONDITION = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
            "分析需求：\n" +
            "{数据分析的需求或者目标}\n" +
            "原始数据：\n" +
            "{csv格式的原始数据，用,作为分隔符}\n" +
            "请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
            "【【【【【\n" +
            "{前端 Echarts V5 的 option 配置对象js代码，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
            "【【【【【\n" +
            "{明确的数据分析结论，越详细越好，不要生成多余的注释\n}" +
            "最终格式是：【【【【【前端代码【【【【【分析结论";

    String PROMPT =
            "user: 你是一名数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容： " +
                    "分析需求： {数据分析的需求或者目标} " +
                    "原始数据： {CSV格式的原始数据，用,作为分隔符} " +
                    "请根据这两部分内容，按照以下指定格式生成内容（此外不需要输出任何多余的开头、结尾、注释） " +
                    "【【【【【 {前端 Echarts V5 的 option 配置对象json代码（不要生成title），合理地将数据进行可视化，不要生成任何多余的内容，比如注释。} " +
                    "【【【【【 {明确的数据分析结论、越详细越好，不要生成多余注释}";


}
