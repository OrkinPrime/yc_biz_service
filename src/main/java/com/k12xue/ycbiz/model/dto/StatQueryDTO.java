package com.k12xue.ycbiz.model.dto;

import lombok.Data;

/**
 * 查询参数对象
 * @author Orkin_Prime
 * @date 2026/1/5 15:01
 */
@Data
public class StatQueryDTO {

    // --- 必填：学校ID ---
    private Long schoolId;

    // --- 前端核心筛选参数 ---

    /**
     * 统计模式
     * values: "time" (按时间段), "day" (按日)
     */
    private String mold;

    /**
     * 开始日期
     * mold=day 时代表具体的“那一天”
     * 示例: "2025-12-08" 或 "0"
     */
    private String start;

    /**
     * 结束日期
     * mold=day 时通常为 "0" 或不传
     * 示例: "2026-01-06" 或 "0"
     */
    private String end;

    /**
     * 年级ID
     * 示例: 66568 或 0 (0代表全部)
     */
    private Integer gradeid;

    /**
     * 学科ID
     * 示例: 1071 或 0 (0代表全部)
     */
    private Long xkid;

    // --- 文本模糊搜索 ---
    private String teacherName;

    // --- 分页参数 ---
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}