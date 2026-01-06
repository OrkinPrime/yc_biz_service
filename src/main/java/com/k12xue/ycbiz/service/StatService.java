package com.k12xue.ycbiz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12xue.ycbiz.model.dto.StatQueryDTO;
import com.k12xue.ycbiz.model.vo.TeacherFuncStatVO;
import java.util.List;

public interface StatService {
    /**
     * 分页查询
     */
    Page<TeacherFuncStatVO> searchStats(StatQueryDTO query);

    /**
     * 获取所有符合条件的数据（用于导出）
     */
    List<TeacherFuncStatVO> getExportData(StatQueryDTO query);
}