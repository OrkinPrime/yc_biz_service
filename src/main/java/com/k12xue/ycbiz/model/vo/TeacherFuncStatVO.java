package com.k12xue.ycbiz.model.vo;

import lombok.Data;

/**
 * 对应前端查询结果
 *
 * @author Orkin_Prime
 * @date 2026/1/5 14:54
 */

@Data
public class TeacherFuncStatVO {
    // --- 全局连贯序号 ---
    private Long serialNumber;

    // --- 基础信息列 ---
    private String realName;     // 教师姓名
    private String subjectName;  // 学科名称
    private String periodName;   // 节次名称
    private String dateStr;      // 日期
    private String className;    // 班级名称

    // --- 核心统计列 ---
    private Integer type1Count = 0; // 随机提问
    private Integer type2Count = 0; // 抢答
    private Integer type3Count = 0; // 限时答题
    private Integer type4Count = 0; // 组题
    private Integer type5Count = 0; // 分组教学
    private Integer type6Count = 0; // 拍照投影
    private Integer type7Count = 0; // 学生打分
    private Integer type8Count = 0; // 小组打分
}
