package com.k12xue.ycbiz.model.dto;

import lombok.Data;

/**
 * 查询参数对象
 * @author Orkin_Prime
 * @date 2026/1/5 15:01
 */
@Data
public class StatQueryDTO {
    // 学校ID
    private Long schoolId;

    // --- 可选筛选 ---
    private Long province;
    private Long city;
    private Long district;

    // 教师姓名
    private String teacherName;

    // --- 时间筛选 ---
    private String startDate;
    private String endDate;

    // --- 分页参数 ---
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}