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
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.apache.poi.ss.util.CellRangeAddress;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 统计服务Controller
 *
 * @author Orkin_Prime
 * @date 2026/1/7 15:04
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

        HorizontalCellStyleStrategy styleStrategy = getTwelveXueStyle();

        SheetWriteHandler sheetConfigHandler = getSheetConfigHandler();

        EasyExcel.write(response.getOutputStream(), TeacherFuncStatVO.class)
                .sheet("十二学统计报表")
                .registerWriteHandler(styleStrategy)
                .registerWriteHandler(sheetConfigHandler)
                .doWrite(list);
    }
    /**
     * 表格样式配置
     */
    private HorizontalCellStyleStrategy getTwelveXueStyle() {
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();

        headWriteCellStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());

        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontName("Microsoft YaHei");
        headWriteFont.setFontHeightInPoints((short) 11); // 字号11，精致不臃肿
        headWriteFont.setBold(true);
        headWriteFont.setColor(IndexedColors.WHITE.getIndex()); // 白色文字
        headWriteCellStyle.setWriteFont(headWriteFont);

        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignmentEnum.CENTER.getPoiHorizontalAlignment());
        headWriteCellStyle.setVerticalAlignment(VerticalAlignmentEnum.CENTER.getPoiVerticalAlignmentEnum());

        headWriteCellStyle.setBorderBottom(BorderStyle.THIN);
        headWriteCellStyle.setBorderLeft(BorderStyle.THIN);
        headWriteCellStyle.setBorderRight(BorderStyle.THIN);
        headWriteCellStyle.setBorderTop(BorderStyle.THIN);

        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();

        WriteFont contentWriteFont = new WriteFont();
        contentWriteFont.setFontName("Microsoft YaHei");
        contentWriteFont.setFontHeightInPoints((short) 10);
        contentWriteFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());
        contentWriteCellStyle.setWriteFont(contentWriteFont);

        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignmentEnum.CENTER.getPoiHorizontalAlignment());
        contentWriteCellStyle.setVerticalAlignment(VerticalAlignmentEnum.CENTER.getPoiVerticalAlignmentEnum());

        contentWriteCellStyle.setBorderBottom(BorderStyle.THIN);
        contentWriteCellStyle.setBorderLeft(BorderStyle.THIN);
        contentWriteCellStyle.setBorderRight(BorderStyle.THIN);
        contentWriteCellStyle.setBorderTop(BorderStyle.THIN);

        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
    }

    /**
     * 表格配置拦截器
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