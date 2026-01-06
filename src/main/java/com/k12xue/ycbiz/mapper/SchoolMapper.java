package com.k12xue.ycbiz.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12xue.ycbiz.model.entity.School;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("operation_db")
public interface SchoolMapper extends BaseMapper<School> {
}
