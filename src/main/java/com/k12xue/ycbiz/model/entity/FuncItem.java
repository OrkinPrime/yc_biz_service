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
 * 课堂小功能使用记录表
 * </p>
 *
 * <p>对应数据库表：op_analysis_func_item</p>
 * <p>主要用于：保存教师使用课堂小功能的记录，结合课堂时间记录表，能够形成某一节课某教师使用课堂小功能的情况</p>
 *
 * @author 李忠斌
 * @since 2026-01-05
 */
@Data
@TableName("op_analysis_func_item")
public class FuncItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long uid;

    private String username;

    private String realname;

    private String xxmc;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableField("schoolid")
    private Long schoolid;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long province;

    @TableField("provincename")
    private String provincename;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableField("city")
    private Long city;

    @TableField("cityname")
    private String cityname;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long district;

    @TableField("districtname")
    private String districtname;

    private String xd;

    private String njmc;

    /**
     * 功能类型
     * DB类型: tinyint(1)
     * 说明: 1随机提问，2抢答，3限时答题，4组题，5分组教学，6拍照投影，7学生打分，8小组打分
     * 策略：虽是 tinyint，但在业务中属于枚举状态，用 Integer 最方便
     */
    private Integer type;

    /**
     * 学科ID
     * DB类型: bigint(20)
     * ⚠️ 风险：必须转 String
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long subjectid;

    /**
     * 所交学科名称
     * DB类型: varchar(100)
     */
    private String subject;

    /**
     * 年
     * DB类型: int(4)
     */
    private Integer year;

    /**
     * 月
     * DB类型: tinyint(2)
     */
    private Integer month;

    /**
     * 周
     * DB类型: tinyint(2)
     */
    private Integer week;

    /**
     * 日
     * DB类型: tinyint(2)
     */
    private Integer day;

    /**
     * 创建时间
     * DB类型: int(11)
     * 策略：这是 Unix 时间戳（秒），不要映射成 Date
     */
    private Long createTime;

    /**
     * 电话号码
     * DB类型: varchar(20)
     */
    private String telephone;

    /**
     * 周次年份
     * DB类型: int(4)
     */
    private Integer weekyear;

}
