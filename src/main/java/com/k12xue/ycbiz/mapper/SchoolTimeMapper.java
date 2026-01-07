package com.k12xue.ycbiz.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12xue.ycbiz.model.entity.SchoolTime;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学校时间表Mapper
 *
 * @author Orkin_Prime
 * @date 2026/1/7 15:13
 */
@Mapper
@DS("k12")
public interface SchoolTimeMapper extends BaseMapper<SchoolTime> {
}
