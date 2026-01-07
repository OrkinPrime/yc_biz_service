package com.k12xue.ycbiz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;


/**
 * <p>
 * 学校信息表
 * </p>
 *
 * <p>对应数据库表：op_analysis_register_school</p>
 * <p>主要用于：保存学校信息，与教师表联合查询</p>
 *
 * @author 李忠斌
 * @since 2026-01-05
 */
@Data
@TableName("op_analysis_register_school")
public class School implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * DB类型: int(10) AUTO_INCREMENT
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 区域ID
     * DB类型: bigint(20)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long area;

    /**
     * 学校ID
     * DB类型: bigint(20)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField("schoolid")
    private Long schoolid;

    /**
     * 学校名称
     * DB类型: varchar(100)
     */
    private String xxmc;

    /**
     * 学校负责人
     * DB类型: varchar(100)
     */
    private String manager;

    /**
     * 课堂类型
     * DB类型: tinyint(1)
     * 注释：3混合式课堂，2平板课堂，1卡片课堂
     */
    @TableField("coursetype")
    private Integer coursetype;

    /**
     * 是否设置学校logo
     * DB类型: tinyint(1)
     * 注释：0未设置，1设置
     */
    private Integer logo;

    /**
     * 是否设置域名
     * DB类型: tinyint(1)
     * 注释：0未设置，1设置
     */
    private Integer domain;

    /**
     * 是否设置校本资源banner
     * DB类型: tinyint(1)
     * 注释：0未设置，1设置
     */
    @TableField("sourcebanner")
    private Integer sourcebanner;

    /**
     * 是否设置教材版本
     * DB类型: tinyint(1)
     * 注释：0未设置，1设置
     */
    private Integer version;

    /**
     * 排序
     * DB类型: bigint(20)
     * ⚠️ 必须转字符串
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sort;

    /**
     * 小学学制
     * DB类型: tinyint(1)
     */
    private Integer xxxz;

    /**
     * 初中学制
     * DB类型: tinyint(1)
     */
    private Integer czxz;

    /**
     * 高中学制
     * DB类型: tinyint(1)
     */
    private Integer gzxz;

    /**
     * 合作深度
     * DB类型: tinyint(1)
     * 注释：1试点，2中银，3B付费，4C付费
     */
    private Integer coper;
}
