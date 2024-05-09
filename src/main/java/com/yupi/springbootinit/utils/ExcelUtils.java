package com.yupi.springbootinit.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.ThrowUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Excel相关工具类
 */
@Slf4j
public class ExcelUtils {
    /**
     * excel转CSV
     * @param multipartFile
     * @return
     */

    public static String excelToCsv(MultipartFile multipartFile){
//        File file = ResourceUtils.getFile("classpath:网站数据.xlsx");
        //读取数据
        List<Map<Integer, String>> list = null;
        try {
            /**
             * 使用EasyExcel库，通过MultipartFile.getInputStream()获取文件流，
             * 指定Excel类型为ExcelTypeEnum.XLSX，
             * 读取第0个工作表（sheet()），
             * 并忽略无标题行（headRowNumber(0)），
             * 同步读取数据。
             */
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("表格处理错误",e);
        }
        if (CollUtil.isEmpty(list)){
            return "";
        }
        /**
         * 将解析得到的Excel数据转换为CSV格式的文本
         */
        StringBuilder sb = new StringBuilder();
        //读取解析后的数据列表，获取首行（表头）的键值对
        LinkedHashMap<Integer,String> headerMap = (LinkedHashMap) list.get(0);
        //从表头中筛选出非空字段名，以逗号分隔拼接成CSV表头行。
        List<String> headerList = headerMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
        sb.append(StringUtils.join(headerList, ",")).append("\n");
        //遍历数据列表，对于每一行数据，筛选出非空字段值，以逗号分隔拼接成CSV数据行
        for (int i = 0; i < list.size(); i++) {
            LinkedHashMap<Integer,String> dataMap = (LinkedHashMap) list.get(i);
            List<String> dataList = dataMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            //将表头行与所有数据行依次追加到StringBuilder中，生成完整的CSV文本。
            sb.append(StringUtils.join(dataList, ",")).append("\n");
        }
        return sb.toString();
    }

//    public static void main(String[] args) {
//        excelToCsv(null);
//    }
    /**
     * 检查文件
     *
     * @param file 文件
     */
    public static void checkExcelFile(MultipartFile file) {
        long size = file.getSize();
        String originalFilename = file.getOriginalFilename();
        // 校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        // 校验文件后缀 aaa.png
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
    }
}
