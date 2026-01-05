package com.k12xue.ycbiz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 存储用户和学校对应关系
 *
 * @author Orkin_Prime
 * @date 2026/1/5 16:18
 */

@Data
@TableName("uia.uc_user_school")
public class UserSchool {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 身份类型
     * 2是老师
     */
    private Integer identitytype;

    /**
     * 学校ID
     */
    private Long schoolid;

    /**
     * 状态
     * 0 或 -1 代表正常/删除
     */
    private Integer status;
}
