package com.k12xue.ycbiz.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12xue.ycbiz.common.Result;
import com.k12xue.ycbiz.model.dto.StatQueryDTO;
import com.k12xue.ycbiz.model.vo.TeacherFuncStatVO;
import com.k12xue.ycbiz.service.StatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 统计Controller
 *
 * @author Orkin_Prime
 * @date 2026/1/5 16:34
 */
@RestController
@RequestMapping("/stat")
@Api(tags = "课堂小功能使用统计模块")
public class StatController {

    @Autowired
    private StatService statService;

    @GetMapping("/teacher/list")
    @ApiOperation(value = "分页查询教师课堂小功能统计")
    public Result<Page<TeacherFuncStatVO>> getTeacherStats(StatQueryDTO queryDTO) {
        // 这里的 queryDTO 会自动接收 URL 中的参数
        // 例如: /stat/teacher/list?schoolId=123&pageNo=1&pageSize=10

        // 可以在这里加简单的参数校验，比如 schoolId 不能为空
        if (queryDTO.getSchoolId() == null) {
            return Result.fail("必须选择一所学校"); // 假设你的 Result 类有 fail 方法
        }

        Page<TeacherFuncStatVO> pageResult = statService.searchStats(queryDTO);

        // 假设你的 Result 类有 success 方法
        return Result.success(pageResult);
    }
}