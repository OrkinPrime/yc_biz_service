package com.k12xue.ycbiz.model.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.*;
import lombok.Data;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.alibaba.excel.enums.poi.VerticalAlignmentEnum;
import java.io.Serializable;

/**
 * 响应数据包
 * @author Orkin_Prime
 * @date 2026/1/8 8:53
 */
@Data
@HeadRowHeight(30)
@ContentRowHeight(25)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER, verticalAlignment = VerticalAlignmentEnum.CENTER)
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER, verticalAlignment = VerticalAlignmentEnum.CENTER)
public class TeacherFuncStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty("序号")
    @ColumnWidth(10)
    private Long serialNumber;

    @ExcelProperty("教师姓名")
    @ColumnWidth(15)
    private String realName;

    @ExcelProperty("所教学科")
    @ColumnWidth(15)
    private String subjectName;

    @ExcelProperty("班级名称")
    @ColumnWidth(20)
    private String className;

    @ExcelProperty("课堂开始时间")
    @ColumnWidth(22)
    private String startTime;

    @ExcelProperty("课堂结束时间")
    @ColumnWidth(22)
    private String endTime;

    @ExcelProperty("课堂时长")
    @ColumnWidth(15)
    private String duration;

    // --- 统计数据 ---

    @ExcelProperty("限时答题")
    @ColumnWidth(12)
    private Integer type3Count = 0;

    @ExcelProperty("组题")
    @ColumnWidth(10)
    private Integer type4Count = 0;

    @ExcelProperty("随机提问")
    @ColumnWidth(12)
    private Integer type1Count = 0;

    @ExcelProperty("抢答")
    @ColumnWidth(10)
    private Integer type2Count = 0;

    @ExcelProperty("学生打分")
    @ColumnWidth(12)
    private Integer type7Count = 0;

    @ExcelProperty("小组打分")
    @ColumnWidth(12)
    private Integer type8Count = 0;

    @ExcelProperty("拍照投影")
    @ColumnWidth(12)
    private Integer type6Count = 0;

    @ExcelProperty("分组教学")
    @ColumnWidth(12)
    private Integer type5Count = 0;
}