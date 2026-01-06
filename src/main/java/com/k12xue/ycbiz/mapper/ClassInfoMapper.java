package com.k12xue.ycbiz.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12xue.ycbiz.model.entity.ClassInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 班级信息Mapper
 *
 * @author Orkin_Prime
 * @date 2026/1/6 13:35
 */
@Mapper
@DS("uia")
public interface ClassInfoMapper extends BaseMapper<ClassInfo> {
}
