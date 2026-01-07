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
 * 课堂时间记录表
 * </p>
 *
 * <p>对应数据库表：op_analysis_func_item</p>
 * <p>主要用于：保存教师使用课堂的时间记录，通过对创建和结束课堂的时间记录，能够获得每节课的时间范围</p>
 *
 * @author 李忠斌
 * @since 2026-01-05
 */
@Data
@TableName("te_course_begin")
public class CourseBegin implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     * DB类型: int(11) unsigned
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     * DB类型: bigint(20) unsigned
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long uid;

    /**
     * 真实姓名
     * DB类型: varchar(50)
     */
    private String realname;

    /**
     * 课程ID
     * DB类型: varchar(50)
     */
    private String bjmc;

    /**
     * 班级ID
     * DB类型: bigint(20)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long classid;

    /**
     * IP地址
     * DB类型: varchar(255)
     */
    private String ip;

    /**
     * 在线状态
     * DB类型: int(2)
     */
    @TableField("isOnline")
    private Integer isOnline;

    /**
     * 创建时间
     * DB类型: int(11) unsigned (Unix 时间戳)
     */
    private Long createTime; // 对应 create_time

    /**
     * 日期时间字符串
     * DB类型: char(50)
     */
    private String dateTime; // 对应 date_time

    /**
     * 结束时间
     * DB类型: int(11)
     */
    private Integer endTime; // 对应 end_time

    /**
     * 是否有答题
     * DB类型: int(11)
     */
    private Integer isAnswer; // 对应 is_answer

    /**
     * 学科ID
     * DB类型: bigint(20)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long xkid;

    /**
     * PC端版本号
     * DB类型: int(11)
     */
    private Integer version;

    /**
     * PC端预览模式
     * DB类型: int(11)
     */
    private Integer pptmode;

}
