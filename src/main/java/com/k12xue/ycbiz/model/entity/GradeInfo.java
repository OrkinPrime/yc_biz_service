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
 * 年级信息表
 * </p>
 *
 * <p>对应数据库表：uia.uc_gradeinfo</p>
 * <p>主要用于：存储学校的年级定义信息</p>
 *
 * @author 李忠斌
 * @since 2026-01-06
 */
@Data
@TableName("uc_gradeinfo")
public class GradeInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     * DB类型: int(10) AUTO_INCREMENT
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 年级名称
     * DB类型: varchar(50)
     */
    private String njmc;

    /**
     * 学校ID
     * DB类型: bigint(20)
     * 策略：BigInt 映射为 Long，并防止前端精度丢失
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long schoolid;

    /**
     * 学段
     * DB类型: varchar(2)
     */
    private String xd;

    /**
     * 创建时间
     * DB类型: int(11)
     * 策略：秒级时间戳
     */
    private Integer createTime;

    /**
     * 更新时间
     * DB类型: int(11)
     */
    private Integer updateTime;

    /**
     * 状态
     * DB类型: tinyint(1)
     * 注释：0正常-1删除
     */
    private Integer status;
}