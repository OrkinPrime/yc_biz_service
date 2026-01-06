package com.k12xue.ycbiz.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12xue.ycbiz.model.entity.ChaptSetting;
import org.apache.ibatis.annotations.Mapper;


/**
 * 科目信息Mapper
 *
 * @author Orkin_Prime
 * @date 2026/1/5 17:15
 */
@Mapper
@DS("k12")
public interface ChaptSettingMapper extends BaseMapper<ChaptSetting> {
}