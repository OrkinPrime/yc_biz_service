package com.k12xue.ycbiz.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12xue.ycbiz.model.entity.CourseBegin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("k12")
public interface CourseBeginMapper extends BaseMapper<CourseBegin> {
}
