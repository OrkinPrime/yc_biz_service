package com.k12xue.ycbiz.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;


/**
 * <p>
 * 学校时间表
 * </p>
 *
 * <p>对应数据库表：k12.te_analysis_schooltime</p>
 * <p>主要用于：保存学校时间表信息，为课堂提供分类依据，如“上午第一节”、“下午第二节”等</p>
 *
 * @author 李忠斌
 * @since 2026-01-05
 */
@Data
@TableName("te_analysis_schooltime")
public class SchoolTime implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableField("schoolid") // 显式指定映射数据库的 schoolid 字段
    private Long schoolid;

    /**
     * 学段
     */
    private Integer xd;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 开始时间 (例如 "08:00")
     */
    private String start;

    /**
     * 结束时间 (例如 "12:00")
     */
    private String end;

    /**
     * 类型 (winter/summer)
     */
    private String type;
}
