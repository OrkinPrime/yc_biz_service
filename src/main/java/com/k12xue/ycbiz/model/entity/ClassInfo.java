package com.k12xue.ycbiz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 班级信息表
 * </p>
 *
 * <p>对应数据库表：uia.uc_classinfo</p>
 * <p>主要用于：存储班级基础信息、班主任及状态设置</p>
 *
 * @author 李忠斌
 * @since 2026-01-06
 */
@Data
@TableName("uc_classinfo")
public class ClassInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 班级ID (主键)
     * DB类型: bigint(20)
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 用户ID
     * DB类型: bigint(20)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long uid;

    /**
     * 学校ID
     * DB类型: bigint(20)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long schoolid;

    /**
     * 年级ID
     * DB类型: int(10)
     */
    private Integer gradeid;

    /**
     * 班级名称
     * DB类型: varchar(100)
     */
    private String classname;

    /**
     * 班级唯一标识
     * DB类型: varchar(20)
     * 注释：用于学生重新选择班级
     */
    private String bjbm;

    /**
     * 班级代码
     * DB类型: int(2)
     * 注释：1、2、3
     */
    private Integer code;

    /**
     * 创建时间
     * DB类型: int(11)
     */
    private Integer createTime;

    /**
     * 更新时间
     * DB类型: int(11)
     */
    private Integer updateTime;

    /**
     * 状态
     * DB类型: tinyint(2)
     * 注释：0 使用 -1 弃用
     */
    private Integer status;

    /**
     * 班级类型
     * DB类型: tinyint(1)
     * 注释：1行政班 2兴趣班
     */
    private Integer type;

    /**
     * 是否是班主任班级
     * DB类型: tinyint(1)
     * 注释：1是 0否
     */
    private Integer isHead;

    /**
     * 考勤打卡
     * DB类型: tinyint(1)
     * 注释：1使用 0不使用
     */
    private Integer useAttendance;

    /**
     * 接收器类型
     * DB类型: tinyint(2)
     */
    private Integer receiveType;

}