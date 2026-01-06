package com.k12xue.ycbiz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12xue.ycbiz.mapper.*;
import com.k12xue.ycbiz.model.dto.StatQueryDTO;
import com.k12xue.ycbiz.model.entity.*;
import com.k12xue.ycbiz.model.vo.TeacherFuncStatVO;
import com.k12xue.ycbiz.service.StatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatServiceImpl implements StatService {

    @Autowired
    private CourseBeginMapper courseBeginMapper;
    @Autowired
    private FuncItemMapper funcItemMapper;
    @Autowired
    private UserSchoolMapper userSchoolMapper;
    @Autowired
    private ChaptSettingMapper chaptSettingMapper;

    // 时间格式化器
    private final DateTimeFormatter fullTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Page<TeacherFuncStatVO> searchStats(StatQueryDTO query) {
        // 1. 获取查询条件 Wrapper
        LambdaQueryWrapper<CourseBegin> courseQuery = buildCommonQuery(query);
        if (courseQuery == null) {
            return new Page<>(query.getPageNo(), query.getPageSize());
        }

        // 2. 分页查询
        Page<CourseBegin> pageParam = new Page<>(query.getPageNo(), query.getPageSize());
        courseBeginMapper.selectPage(pageParam, courseQuery);
        List<CourseBegin> courseList = pageParam.getRecords();

        if (courseList.isEmpty()) {
            return new Page<>(query.getPageNo(), query.getPageSize());
        }

        // 3. 转换 VO
        long baseSerial = (pageParam.getCurrent() - 1) * pageParam.getSize() + 1;
        List<TeacherFuncStatVO> voList = convertToVoList(courseList, query.getSchoolId(), baseSerial, query);

        // 4. 返回分页结果
        Page<TeacherFuncStatVO> resultPage = new Page<>();
        resultPage.setCurrent(pageParam.getCurrent());
        resultPage.setSize(pageParam.getSize());
        resultPage.setTotal(pageParam.getTotal());
        resultPage.setRecords(voList);

        return resultPage;
    }

    @Override
    public List<TeacherFuncStatVO> getExportData(StatQueryDTO query) {
        // 1. 获取查询条件 Wrapper
        LambdaQueryWrapper<CourseBegin> courseQuery = buildCommonQuery(query);
        if (courseQuery == null) {
            return Collections.emptyList();
        }

        // 2. 查询全部数据 (selectList)
        List<CourseBegin> courseList = courseBeginMapper.selectList(courseQuery);

        if (courseList.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 转换 VO
        return convertToVoList(courseList, query.getSchoolId(), 1L, query);
    }

    // ---------------------------------------------------------
    // 内部通用逻辑抽取
    // ---------------------------------------------------------

    /**
     * 构建通用的 CourseBegin 查询条件
     * 返回 null 表示前置条件不满足（如没有老师）
     */
    private LambdaQueryWrapper<CourseBegin> buildCommonQuery(StatQueryDTO query) {
        // 1. 获取该学校的教师ID列表
        LambdaQueryWrapper<UserSchool> relationQuery = new LambdaQueryWrapper<>();
        relationQuery.select(UserSchool::getUid)
                .eq(UserSchool::getSchoolid, query.getSchoolId())
                .eq(UserSchool::getIdentitytype, 2)
                .eq(UserSchool::getStatus, 0);

        List<Object> uidObjs = userSchoolMapper.selectObjs(relationQuery);
        if (uidObjs == null || uidObjs.isEmpty()) {
            return null;
        }
        List<Long> schoolUids = uidObjs.stream()
                .map(obj -> Long.valueOf(obj.toString()))
                .collect(Collectors.toList());

        // 2. 准备时间范围
        long startTs = 0;
        long endTs = System.currentTimeMillis() / 1000;
        if (StringUtils.hasText(query.getStartDate())) {
            startTs = LocalDate.parse(query.getStartDate()).atStartOfDay(ZoneOffset.of("+8")).toEpochSecond();
        }
        if (StringUtils.hasText(query.getEndDate())) {
            endTs = LocalDate.parse(query.getEndDate()).plusDays(1).atStartOfDay(ZoneOffset.of("+8")).toEpochSecond() - 1;
        }

        // 3. 组装 CourseBegin 查询
        LambdaQueryWrapper<CourseBegin> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CourseBegin::getUid, schoolUids);
        wrapper.ge(CourseBegin::getCreateTime, startTs);
        wrapper.le(CourseBegin::getCreateTime, endTs);
        if (StringUtils.hasText(query.getTeacherName())) {
            wrapper.like(CourseBegin::getRealname, query.getTeacherName());
        }
        wrapper.orderByDesc(CourseBegin::getCreateTime);

        return wrapper;
    }

    /**
     * 核心转换逻辑：将 CourseBegin 列表转换为 VO 列表
     * 同时处理了 关联查询（Subjects, FuncItems）和 数据组装
     */
    private List<TeacherFuncStatVO> convertToVoList(List<CourseBegin> courseList, Long schoolId, long startSerial, StatQueryDTO query) {
        // 1. 准备辅助数据 - ID 提取
        List<Long> uids = courseList.stream().map(CourseBegin::getUid).distinct().collect(Collectors.toList());
        List<Long> subjectIds = courseList.stream().map(CourseBegin::getXkid).filter(Objects::nonNull).distinct().collect(Collectors.toList());

        // 2. 批量查询学科名称
        Map<Long, String> subjectMap = Collections.emptyMap();
        if (!subjectIds.isEmpty()) {
            List<ChaptSetting> settings = chaptSettingMapper.selectList(new LambdaQueryWrapper<ChaptSetting>()
                    .eq(ChaptSetting::getSchoolid, schoolId)
                    .in(ChaptSetting::getSubjectid, subjectIds));
            subjectMap = settings.stream().collect(Collectors.toMap(
                    ChaptSetting::getSubjectid, ChaptSetting::getName, (v1, v2) -> v1));
        }

        // 3. 批量查询功能记录 (注意时间范围要与 Query 保持一致，优化查询效率)
        long queryStartTs = 0;
        long queryEndTs = System.currentTimeMillis() / 1000 + 86400; // 稍微宽泛一点防止边缘误差
        if (StringUtils.hasText(query.getStartDate())) {
            queryStartTs = LocalDate.parse(query.getStartDate()).atStartOfDay(ZoneOffset.of("+8")).toEpochSecond();
        }

        List<FuncItem> funcItems = funcItemMapper.selectList(new LambdaQueryWrapper<FuncItem>()
                .in(FuncItem::getUid, uids)
                .ge(FuncItem::getCreateTime, queryStartTs)
                .le(FuncItem::getCreateTime, queryEndTs)); // 使用外部传入的时间范围粗筛

        Map<Long, List<FuncItem>> funcMap = funcItems.stream().collect(Collectors.groupingBy(FuncItem::getUid));

        // 4. 循环组装
        List<TeacherFuncStatVO> resultList = new ArrayList<>(courseList.size());
        for (int i = 0; i < courseList.size(); i++) {
            CourseBegin course = courseList.get(i);
            TeacherFuncStatVO vo = new TeacherFuncStatVO();

            vo.setSerialNumber(startSerial + i);
            vo.setRealName(course.getRealname());
            vo.setClassName(course.getBjmc() != null ? course.getBjmc() : "");

            // 学科
            String subName = subjectMap.get(course.getXkid());
            if (subName == null) subName = course.getXkid() != null ? String.valueOf(course.getXkid()) : "";
            vo.setSubjectName(subName);

            // --- 时间计算逻辑 ---
            long start = course.getCreateTime();
            long end = (course.getEndTime() == null) ? start + 2400 : Long.valueOf(course.getEndTime());
            // 防御性逻辑：如果 end 存的是时长或异常小，进行修正
            if (end < start) {
                end = start + end;
            }

            LocalDateTime startDt = LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.of("+8"));
            LocalDateTime endDt = LocalDateTime.ofEpochSecond(end, 0, ZoneOffset.of("+8"));

            vo.setStartTime(fullTimeFmt.format(startDt));
            vo.setEndTime(fullTimeFmt.format(endDt));

            // 计算时长 (分钟)
            long durationMinutes = (end - start) / 60;
            vo.setDuration(durationMinutes + "分钟");

            // --- 统计功能次数 ---
            List<FuncItem> ops = funcMap.getOrDefault(course.getUid(), Collections.emptyList());
            // 传入精确的 start 和 end 进行过滤
            countFunctions(vo, start, end, ops);

            resultList.add(vo);
        }
        return resultList;
    }

    /**
     * 统计具体功能的次数
     */
    private void countFunctions(TeacherFuncStatVO vo, long start, long end, List<FuncItem> ops) {
        for (FuncItem op : ops) {
            long opTime = op.getCreateTime();
            // 精确匹配：操作时间必须在课堂开始和结束之间
            if (opTime >= start && opTime <= end) {
                Integer type = op.getType();
                if (type == null) continue;
                switch (type) {
                    case 1: vo.setType1Count(vo.getType1Count() + 1); break;
                    case 2: vo.setType2Count(vo.getType2Count() + 1); break;
                    case 3: vo.setType3Count(vo.getType3Count() + 1); break;
                    case 4: vo.setType4Count(vo.getType4Count() + 1); break;
                    case 5: vo.setType5Count(vo.getType5Count() + 1); break;
                    case 6: vo.setType6Count(vo.getType6Count() + 1); break;
                    case 7: vo.setType7Count(vo.getType7Count() + 1); break;
                    case 8: vo.setType8Count(vo.getType8Count() + 1); break;
                }
            }
        }
    }
}