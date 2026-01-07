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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计功能服务类
 * @author Orkin_Prime
 * @date 2026/1/7 15:04
 */
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
    @Autowired
    private ClassInfoMapper classInfoMapper;

    private final DateTimeFormatter fullTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Page<TeacherFuncStatVO> searchStats(StatQueryDTO query) {
        // 1. 构建通用查询条件
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

        // 3. 转换数据
        long baseSerial = (pageParam.getCurrent() - 1) * pageParam.getSize() + 1;
        List<TeacherFuncStatVO> voList = convertToVoList(courseList, query.getSchoolId(), baseSerial, query);

        Page<TeacherFuncStatVO> resultPage = new Page<>();
        resultPage.setCurrent(pageParam.getCurrent());
        resultPage.setSize(pageParam.getSize());
        resultPage.setTotal(pageParam.getTotal());
        resultPage.setRecords(voList);

        return resultPage;
    }

    @Override
    public List<TeacherFuncStatVO> getExportData(StatQueryDTO query) {
        LambdaQueryWrapper<CourseBegin> courseQuery = buildCommonQuery(query);
        if (courseQuery == null) {
            return Collections.emptyList();
        }

        List<CourseBegin> courseList = courseBeginMapper.selectList(courseQuery);
        if (courseList.isEmpty()) {
            return Collections.emptyList();
        }

        return convertToVoList(courseList, query.getSchoolId(), 1L, query);
    }

    private LambdaQueryWrapper<CourseBegin> buildCommonQuery(StatQueryDTO query) {
        // --- 1. 基础范围：学校下的有效教师 ---
        if (query.getSchoolId() == null) {
            // 防御性检查，虽然前端参数没列，但必须有
            return null;
        }
        LambdaQueryWrapper<UserSchool> relationQuery = new LambdaQueryWrapper<>();
        relationQuery.select(UserSchool::getUid)
                .eq(UserSchool::getSchoolid, query.getSchoolId())
                .eq(UserSchool::getIdentitytype, 2)
                .eq(UserSchool::getStatus, 0);
        List<Object> uidObjs = userSchoolMapper.selectObjs(relationQuery);
        if (CollectionUtils.isEmpty(uidObjs)) {
            return null;
        }
        List<Long> schoolUids = uidObjs.stream().map(obj -> Long.valueOf(obj.toString())).collect(Collectors.toList());

        LambdaQueryWrapper<CourseBegin> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CourseBegin::getUid, schoolUids);

        // --- 2. 处理时间筛选 (mold = time / day) ---
        long startTs = 0;
        long endTs = System.currentTimeMillis() / 1000;

        String startDateStr = parseParamDate(query.getStart());
        String endDateStr = parseParamDate(query.getEnd());
        String mold = query.getMold();

        if ("day".equalsIgnoreCase(mold)) {
            // 按日模式：start 代表具体的某一天
            if (startDateStr != null) {
                startTs = LocalDate.parse(startDateStr).atStartOfDay(ZoneOffset.of("+8")).toEpochSecond();
                // 结束时间为当天的最后一秒
                endTs = LocalDate.parse(startDateStr).plusDays(1).atStartOfDay(ZoneOffset.of("+8")).toEpochSecond() - 1;
            }
        } else {
            // 按时间段模式 (默认)
            if (startDateStr != null) {
                startTs = LocalDate.parse(startDateStr).atStartOfDay(ZoneOffset.of("+8")).toEpochSecond();
            }
            if (endDateStr != null) {
                endTs = LocalDate.parse(endDateStr).plusDays(1).atStartOfDay(ZoneOffset.of("+8")).toEpochSecond() - 1;
            }
        }
        wrapper.ge(CourseBegin::getCreateTime, startTs);
        wrapper.le(CourseBegin::getCreateTime, endTs);

        // --- 3. 学科筛选 (xkid) ---
        // 0 代表全部，非 0 代表具体学科
        if (query.getXkid() != null && query.getXkid() != 0) {
            wrapper.eq(CourseBegin::getXkid, query.getXkid());
        }

        // --- 4. 年级筛选 (gradeid) ---
        // 0 代表全部，非 0 代表具体年级
        if (query.getGradeid() != null && query.getGradeid() != 0) {
            // 直接根据 gradeid 查出该年级下所有的 classId
            List<Long> classIds = findClassIdsByGradeId(query.getSchoolId(), query.getGradeid());
            if (CollectionUtils.isEmpty(classIds)) {
                return null; // 该年级下没有班级，结果肯定为空
            }
            wrapper.in(CourseBegin::getClassid, classIds);
        }

        // --- 5. 教师姓名模糊搜索 ---
        if (StringUtils.hasText(query.getTeacherName())) {
            wrapper.like(CourseBegin::getRealname, query.getTeacherName());
        }

        wrapper.orderByDesc(CourseBegin::getCreateTime);
        return wrapper;
    }

    /**
     * 辅助方法：解析前端传入的日期字符串
     * 处理 "0", null, "" 的情况
     */
    private String parseParamDate(String dateParam) {
        if (!StringUtils.hasText(dateParam) || "0".equals(dateParam)) {
            return null;
        }
        return dateParam;
    }

    /**
     * 辅助方法：根据 gradeid 查找对应的班级ID列表
     */
    private List<Long> findClassIdsByGradeId(Long schoolId, Integer gradeId) {
        LambdaQueryWrapper<ClassInfo> classQ = new LambdaQueryWrapper<>();
        classQ.select(ClassInfo::getId)
                .eq(ClassInfo::getSchoolid, schoolId)
                .eq(ClassInfo::getGradeid, gradeId); // 直接匹配 gradeid

        // 如果只统计现有有效班级，可以加 .eq(ClassInfo::getStatus, 0);
        // 但通常统计报表需要包含已毕业/已删除班级的历史数据，建议不加 status 限制

        List<Object> classIdObjs = classInfoMapper.selectObjs(classQ);
        if (CollectionUtils.isEmpty(classIdObjs)) {
            return Collections.emptyList();
        }

        return classIdObjs.stream()
                .map(obj -> Long.valueOf(obj.toString()))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    // 下面是 convertToVoList，需要同步更新时间过滤逻辑，确保统计次数准确
    // ---------------------------------------------------------

    private List<TeacherFuncStatVO> convertToVoList(List<CourseBegin> courseList, Long schoolId, long startSerial, StatQueryDTO query) {
        List<Long> uids = courseList.stream().map(CourseBegin::getUid).distinct().collect(Collectors.toList());
        List<Long> subjectIds = courseList.stream().map(CourseBegin::getXkid).filter(Objects::nonNull).distinct().collect(Collectors.toList());

        Map<Long, String> subjectMap = Collections.emptyMap();
        if (!subjectIds.isEmpty()) {
            List<ChaptSetting> settings = chaptSettingMapper.selectList(new LambdaQueryWrapper<ChaptSetting>()
                    .eq(ChaptSetting::getSchoolid, schoolId)
                    .in(ChaptSetting::getSubjectid, subjectIds));
            subjectMap = settings.stream().collect(Collectors.toMap(
                    ChaptSetting::getSubjectid, ChaptSetting::getName, (v1, v2) -> v1));
        }

        // --- 计算查询FuncItem的时间范围 ---
        // 这里的逻辑要和 buildCommonQuery 保持一致，避免查出范围外的数据
        long queryStartTs = 0;
        long queryEndTs = System.currentTimeMillis() / 1000 + 86400;

        String startDateStr = parseParamDate(query.getStart());
        String endDateStr = parseParamDate(query.getEnd());
        String mold = query.getMold();

        if ("day".equalsIgnoreCase(mold) && startDateStr != null) {
            queryStartTs = LocalDate.parse(startDateStr).atStartOfDay(ZoneOffset.of("+8")).toEpochSecond();
            queryEndTs = LocalDate.parse(startDateStr).plusDays(1).atStartOfDay(ZoneOffset.of("+8")).toEpochSecond();
        } else {
            if (startDateStr != null) {
                queryStartTs = LocalDate.parse(startDateStr).atStartOfDay(ZoneOffset.of("+8")).toEpochSecond();
            }
            if (endDateStr != null) {
                queryEndTs = LocalDate.parse(endDateStr).plusDays(1).atStartOfDay(ZoneOffset.of("+8")).toEpochSecond();
            }
        }

        // 稍微放宽一点 FuncItem 的查询范围，确保边界数据不丢失，具体的精确过滤由 countFunctions 方法处理
        List<FuncItem> funcItems = funcItemMapper.selectList(new LambdaQueryWrapper<FuncItem>()
                .in(FuncItem::getUid, uids)
                .ge(FuncItem::getCreateTime, queryStartTs)
                .le(FuncItem::getCreateTime, queryEndTs));

        Map<Long, List<FuncItem>> funcMap = funcItems.stream().collect(Collectors.groupingBy(FuncItem::getUid));

        List<TeacherFuncStatVO> resultList = new ArrayList<>(courseList.size());

        for (int i = 0; i < courseList.size(); i++) {
            CourseBegin course = courseList.get(i);
            TeacherFuncStatVO vo = new TeacherFuncStatVO();
            vo.setSerialNumber(startSerial + i);
            vo.setRealName(course.getRealname());
            vo.setClassName(course.getBjmc() != null ? course.getBjmc() : "");

            String subName = subjectMap.get(course.getXkid());
            if (subName == null) subName = course.getXkid() != null ? String.valueOf(course.getXkid()) : "";
            vo.setSubjectName(subName);

            long start = course.getCreateTime();
            long end = (course.getEndTime() == null) ? start + 2400 : Long.valueOf(course.getEndTime());
            if (end < start) end = start + end;

            LocalDateTime startDt = LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.of("+8"));
            LocalDateTime endDt = LocalDateTime.ofEpochSecond(end, 0, ZoneOffset.of("+8"));

            vo.setStartTime(fullTimeFmt.format(startDt));
            vo.setEndTime(fullTimeFmt.format(endDt));

            long durationSeconds = end - start;
            if (durationSeconds < 0) durationSeconds = 0;
            long durationMinutes = durationSeconds / 60;
            vo.setDuration(durationMinutes + "分钟");

            List<FuncItem> ops = funcMap.getOrDefault(course.getUid(), Collections.emptyList());
            countFunctions(vo, start, end, ops); // 精确过滤

            resultList.add(vo);
        }
        return resultList;
    }

    private void countFunctions(TeacherFuncStatVO vo, long start, long end, List<FuncItem> ops) {
        for (FuncItem op : ops) {
            long opTime = op.getCreateTime();
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