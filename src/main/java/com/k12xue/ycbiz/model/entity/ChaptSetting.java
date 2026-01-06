package com.k12xue.ycbiz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;


/**
 * 科目信息
 *
 * @author Orkin_Prime
 * @date 2026/1/5 17:14
 */
@Data
@TableName("te_chapt_setting")
public class ChaptSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long schoolid;

    /**
     * 学科显示名称 (例如：语文)
     */
    private String name;

    /**
     * 学科id (例如：1071)
     */
    private Long subjectid;

    private Integer sort;

    private String xd;
}