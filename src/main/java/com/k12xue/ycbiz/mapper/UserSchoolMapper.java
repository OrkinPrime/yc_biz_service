package com.k12xue.ycbiz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12xue.ycbiz.model.entity.UserSchool;
import org.apache.ibatis.annotations.Mapper;


/**
 * 针对用户学校表的查询Mapper
 *
 * @author Orkin_Prime
 * @date 2026/1/5 16:20
 */
@Mapper
public interface UserSchoolMapper extends BaseMapper<UserSchool> {
}
