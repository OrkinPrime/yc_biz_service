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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatServiceImpl implements StatService {

    @Autowired
    private CourseBeginMapper courseBeginMapper;
    @Autowired
    private FuncItemMapper funcItemMapper;
    @Autowired
    private SchoolTimeMapper schoolTimeMapper;
    @Autowired
    private UserSchoolMapper userSchoolMapper;
    @Autowired
    private ChaptSettingMapper chaptSettingMapper;

    @Override
    public Page<TeacherFuncStatVO> searchStats(StatQueryDTO query) {
        // ---------------------------------------------------------
        // 1. 核心逻辑：获取该学校的教师ID列表 (UIDs)
        // ---------------------------------------------------------

        LambdaQueryWrapper<UserSchool> relationQuery = new LambdaQueryWrapper<>();
        relationQuery.select(UserSchool::getUid)
                .eq(UserSchool::getSchoolid, query.getSchoolId());

        // 【注意】请确认数据库里的值是否真的是 2 和 0
        relationQuery.eq(UserSchool::getIdentitytype, 2);
        relationQuery.eq(UserSchool::getStatus, 0);

        List<Object> uidObjs = userSchoolMapper.selectObjs(relationQuery);

        if (uidObjs == null || uidObjs.isEmpty()) {
            // 【修正1】去掉 (Long) 强转，直接传 query.getPageNo() 即可
            return new Page<>(query.getPageNo(), query.getPageSize());
        }

        // 这里的 Long.valueOf(obj.toString()) 写法是非常正确且安全的
        List<Long> schoolUids = uidObjs.stream()
                .map(obj -> Long.valueOf(obj.toString()))
                .collect(Collectors.toList());

        // ---------------------------------------------------------
        // 2. 准备查询时间范围
        // ---------------------------------------------------------
        long startTs = 0;
        long endTs = System.currentTimeMillis() / 1000;

        if (StringUtils.hasText(query.getStartDate())) {
            startTs = LocalDate.parse(query.getStartDate())
                    .atStartOfDay(ZoneOffset.of("+8")).toEpochSecond();
        }
        if (StringUtils.hasText(query.getEndDate())) {
            endTs = LocalDate.parse(query.getEndDate())
                    .plusDays(1).atStartOfDay(ZoneOffset.of("+8")).toEpochSecond() - 1;
        }

        // ---------------------------------------------------------
        // 3. 分页查询 CourseBegin
        // ---------------------------------------------------------
        // 【修正2】去掉 (Long) 强转
        Page<CourseBegin> pageParam = new Page<>(query.getPageNo(), query.getPageSize());
        LambdaQueryWrapper<CourseBegin> courseQuery = new LambdaQueryWrapper<>();

        courseQuery.in(CourseBegin::getUid, schoolUids);
        courseQuery.ge(CourseBegin::getCreateTime, startTs);
        courseQuery.le(CourseBegin::getCreateTime, endTs);

        if (StringUtils.hasText(query.getTeacherName())) {
            courseQuery.like(CourseBegin::getRealname, query.getTeacherName());
        }

        courseQuery.orderByDesc(CourseBegin::getCreateTime);

        Page<CourseBegin> coursePage = courseBeginMapper.selectPage(pageParam, courseQuery);
        List<CourseBegin> courseList = coursePage.getRecords();

        if (courseList.isEmpty()) {
            // 【修正3】去掉 (Long) 强转
            return new Page<>(query.getPageNo(), query.getPageSize());
        }

        // ---------------------------------------------------------
        // 4. 准备辅助数据
        // ---------------------------------------------------------

        // 4.1 获取本页涉及的 teacherIds 和 subjectIds
        List<Long> pageUids = courseList.stream().map(CourseBegin::getUid).distinct().collect(Collectors.toList());

        // 【新增】提取本页所有出现的学科ID (去重 + 非空过滤)
        List<Long> subjectIds = courseList.stream()
                .map(CourseBegin::getXkid)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        // 4.2 查功能记录 (保持不变)
        List<FuncItem> funcItems = funcItemMapper.selectList(new LambdaQueryWrapper<FuncItem>()
                .in(FuncItem::getUid, pageUids)
                .ge(FuncItem::getCreateTime, startTs)
                .le(FuncItem::getCreateTime, endTs));

        Map<Long, List<FuncItem>> funcMap = funcItems.stream()
                .collect(Collectors.groupingBy(FuncItem::getUid));

        // 4.3 查学校作息 (保持不变)
        List<SchoolTime> schoolTimes = schoolTimeMapper.selectList(new LambdaQueryWrapper<SchoolTime>()
                .eq(SchoolTime::getSchoolid, query.getSchoolId())
                .orderByAsc(SchoolTime::getSort));

        // ==================【新增：查询学科名称字典】==================
        Map<Long, String> subjectMap = Collections.emptyMap();
        if (!subjectIds.isEmpty()) {
            // 查询 te_chapt_setting 表
            List<ChaptSetting> settings = chaptSettingMapper.selectList(new LambdaQueryWrapper<ChaptSetting>()
                    .eq(ChaptSetting::getSchoolid, query.getSchoolId()) // 必须限制学校，因为不同学校对同一个ID可能有不同叫法
                    .in(ChaptSetting::getSubjectid, subjectIds));      // 只查本页涉及的学科

            // 转为 Map<SubjectID, Name>
            // 注意：如果同一个学校同一个ID有多个记录(比如分学段)，这里简单的取第一个，或者你可以根据业务逻辑去重
            subjectMap = settings.stream()
                    .collect(Collectors.toMap(
                            ChaptSetting::getSubjectid,
                            ChaptSetting::getName,
                            (name1, name2) -> name1 // 如果有重复，取第一个
                    ));
        }
        // ============================================================

        // ---------------------------------------------------------
        // 5. 组装 VO
        // ---------------------------------------------------------
        List<TeacherFuncStatVO> resultList = new ArrayList<>();
        long baseSerial = (coursePage.getCurrent() - 1) * coursePage.getSize() + 1;

        for (int i = 0; i < courseList.size(); i++) {
            CourseBegin course = courseList.get(i);
            TeacherFuncStatVO vo = new TeacherFuncStatVO();

            vo.setSerialNumber(baseSerial + i);
            vo.setRealName(course.getRealname());

            // ==================【新增赋值代码】==================
            // 直接从 CourseBegin 取 bjmc (班级名称)
            // 如果数据库里偶尔有 null，给个默认值空字符串
            vo.setClassName(course.getBjmc() != null ? course.getBjmc() : "");
            // ==================================================

            // ... 学科名称的处理逻辑保持不变 ...
            String subName = subjectMap.get(course.getXkid());
            if (subName == null) {
                subName = course.getXkid() != null ? String.valueOf(course.getXkid()) : "";
            }
            vo.setSubjectName(subName);
            // ============================================================

            LocalDateTime courseTime = LocalDateTime.ofEpochSecond(course.getCreateTime(), 0, ZoneOffset.of("+8"));
            vo.setDateStr(courseTime.toLocalDate().toString());
            vo.setPeriodName(matchPeriod(courseTime.toLocalTime(), schoolTimes));

            List<FuncItem> ops = funcMap.getOrDefault(course.getUid(), Collections.emptyList());
            countFunctions(vo, course, ops);

            resultList.add(vo);
        }

        // 6. 返回结果
        Page<TeacherFuncStatVO> resultPage = new Page<>();
        resultPage.setCurrent(coursePage.getCurrent());
        resultPage.setSize(coursePage.getSize());
        resultPage.setTotal(coursePage.getTotal());
        resultPage.setRecords(resultList);

        return resultPage;
    }

    // --- 内部辅助方法 (保持不变) ---
    private String matchPeriod(LocalTime courseTime, List<SchoolTime> times) {
        if (times == null || times.isEmpty()) return "未知节次";
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        for (SchoolTime t : times) {
            try {
                LocalTime stdStart = LocalTime.parse(t.getStart(), timeFmt);
                LocalTime stdEnd = LocalTime.parse(t.getEnd(), timeFmt);
                if (courseTime.isAfter(stdStart.minusMinutes(15)) && courseTime.isBefore(stdEnd)) {
                    return "第" + t.getSort() + "节";
                }
            } catch (Exception e) {
                // 忽略解析错误
            }
        }
        return "其他时间";
    }

    private void countFunctions(TeacherFuncStatVO vo, CourseBegin course, List<FuncItem> ops) {
        long start = course.getCreateTime();
        // 这里的处理逻辑很好，避免了 NullPointerException
        long end = (course.getEndTime() == null) ? start + 2400 : Long.valueOf(course.getEndTime());
        if (end < start) {
            end = start + end;
        }

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