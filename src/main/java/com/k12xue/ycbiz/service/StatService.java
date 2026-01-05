package com.k12xue.ycbiz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12xue.ycbiz.model.dto.StatQueryDTO;
import com.k12xue.ycbiz.model.vo.TeacherFuncStatVO;

/**
 * 统计服务
 *
 * @author Orkin_Prime
 * @date 2026/1/5 15:39
 */
public interface StatService {
    Page<TeacherFuncStatVO> searchStats(StatQueryDTO query);
}
