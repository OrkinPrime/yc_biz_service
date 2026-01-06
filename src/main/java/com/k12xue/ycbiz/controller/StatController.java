package com.k12xue.ycbiz.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.alibaba.excel.enums.poi.VerticalAlignmentEnum;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12xue.ycbiz.common.Result;
import com.k12xue.ycbiz.model.dto.StatQueryDTO;
import com.k12xue.ycbiz.model.vo.TeacherFuncStatVO;
import com.k12xue.ycbiz.service.StatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// 用于定义筛选区域
import org.apache.poi.ss.util.CellRangeAddress;
// 拦截器接口
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/stat")
@Api(tags = "课堂小功能使用统计模块")
public class StatController {

    @Autowired
    private StatService statService;

    @GetMapping("/teacher/list")
    @ApiOperation(value = "分页查询教师课堂小功能统计")
    public Result<Page<TeacherFuncStatVO>> getTeacherStats(StatQueryDTO queryDTO) {
        if (queryDTO.getSchoolId() == null) {
            return Result.fail("必须选择一所学校");
        }
        Page<TeacherFuncStatVO> pageResult = statService.searchStats(queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/teacher/export")
    @ApiOperation(value = "导出教师课堂小功能统计数据")
    public void exportTeacherStats(StatQueryDTO queryDTO, HttpServletResponse response) throws IOException {
        if (queryDTO.getSchoolId() == null) {
            response.sendError(500, "必须选择一所学校");
            return;
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("教师课堂功能使用统计", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx;filename*=utf-8''" + fileName + ".xlsx");

        List<TeacherFuncStatVO> list = statService.getExportData(queryDTO);

        // 1. 获取“十二学品牌风格”的样式策略 (定制版)
        HorizontalCellStyleStrategy styleStrategy = getTwelveXueStyle();

        // 2. 表格配置拦截器 (保持冻结和筛选不变，这很符合用户习惯)
        SheetWriteHandler sheetConfigHandler = getSheetConfigHandler();

        EasyExcel.write(response.getOutputStream(), TeacherFuncStatVO.class)
                .sheet("十二学统计报表") // Sheet名也改得更有品牌感
                .registerWriteHandler(styleStrategy)
                .registerWriteHandler(sheetConfigHandler)
                .doWrite(list);
    }

    /**
     * 【定制】十二学品牌风格样式
     * 设计思路：
     * 1. 提取网站顶栏的青绿色作为表头背景
     * 2. 配合白色文字，还原网站导航栏的视觉感受
     * 3. 内容区保持干净的白底黑字，符合教育类软件的严谨
     */
    private HorizontalCellStyleStrategy getTwelveXueStyle() {
        // --- 表头样式 (Head) ---
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();

        // 【关键点】背景色：使用海绿色 (SEA_GREEN)
        // 这是 Excel 标准色板中，最接近您网站顶栏那个清新的青绿色的颜色
        headWriteCellStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());

        // 字体：白色、加粗、微软雅黑
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontName("Microsoft YaHei");
        headWriteFont.setFontHeightInPoints((short) 11); // 字号11，精致不臃肿
        headWriteFont.setBold(true);
        headWriteFont.setColor(IndexedColors.WHITE.getIndex()); // 白色文字
        headWriteCellStyle.setWriteFont(headWriteFont);

        // 对齐
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignmentEnum.CENTER.getPoiHorizontalAlignment());
        headWriteCellStyle.setVerticalAlignment(VerticalAlignmentEnum.CENTER.getPoiVerticalAlignmentEnum());

        // 边框：使用同色系的边框或细白边框，这里用细线显得精致
        headWriteCellStyle.setBorderBottom(BorderStyle.THIN);
        headWriteCellStyle.setBorderLeft(BorderStyle.THIN);
        headWriteCellStyle.setBorderRight(BorderStyle.THIN);
        headWriteCellStyle.setBorderTop(BorderStyle.THIN);

        // --- 内容样式 (Content) ---
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();

        // 字体：深灰色 (GREY_80_PERCENT) 比纯黑更柔和，符合现代网页设计规范
        WriteFont contentWriteFont = new WriteFont();
        contentWriteFont.setFontName("Microsoft YaHei");
        contentWriteFont.setFontHeightInPoints((short) 10);
        contentWriteFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());
        contentWriteCellStyle.setWriteFont(contentWriteFont);

        // 对齐
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignmentEnum.CENTER.getPoiHorizontalAlignment());
        contentWriteCellStyle.setVerticalAlignment(VerticalAlignmentEnum.CENTER.getPoiVerticalAlignmentEnum());

        // 边框
        contentWriteCellStyle.setBorderBottom(BorderStyle.THIN);
        contentWriteCellStyle.setBorderLeft(BorderStyle.THIN);
        contentWriteCellStyle.setBorderRight(BorderStyle.THIN);
        contentWriteCellStyle.setBorderTop(BorderStyle.THIN);

        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
    }

    /**
     * 表格配置拦截器 (保持不变，功能性配置)
     */
    private SheetWriteHandler getSheetConfigHandler() {
        return new SheetWriteHandler() {
            @Override
            public void beforeSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {}

            @Override
            public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
                org.apache.poi.ss.usermodel.Sheet sheet = writeSheetHolder.getSheet();
                CellRangeAddress filterRange = new CellRangeAddress(0, 0, 0, 14);
                sheet.setAutoFilter(filterRange);
                sheet.createFreezePane(0, 1);
            }
        };
    }
}