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
import java.util.*;
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

    // --- 新增 Mapper ---
    @Autowired
    private ClassInfoMapper classInfoMapper;
    @Autowired
    private GradeInfoMapper gradeInfoMapper;

    @Override
    public Page<TeacherFuncStatVO> searchStats(StatQueryDTO query) {
        // 1. 获取该学校的教师ID列表 (UIDs)
        LambdaQueryWrapper<UserSchool> relationQuery = new LambdaQueryWrapper<>();
        relationQuery.select(UserSchool::getUid)
                .eq(UserSchool::getSchoolid, query.getSchoolId())
                .eq(UserSchool::getIdentitytype, 2)
                .eq(UserSchool::getStatus, 0);

        List<Object> uidObjs = userSchoolMapper.selectObjs(relationQuery);
        if (uidObjs == null || uidObjs.isEmpty()) {
            return new Page<>(query.getPageNo(), query.getPageSize());
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

        // 3. 分页查询 CourseBegin
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
            return new Page<>(query.getPageNo(), query.getPageSize());
        }

        // ---------------------------------------------------------
        // 4. 准备辅助数据 (批量查询以避免循环查库)
        // ---------------------------------------------------------

        List<Long> pageUids = courseList.stream().map(CourseBegin::getUid).distinct().collect(Collectors.toList());

        // 4.1 提取 ClassId 并查询 ClassInfo
        List<Long> classIds = courseList.stream()
                .map(CourseBegin::getClassid)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, ClassInfo> classInfoMap = new HashMap<>();
        Map<Integer, GradeInfo> gradeInfoMap = new HashMap<>();

        if (!classIds.isEmpty()) {
            List<ClassInfo> classes = classInfoMapper.selectBatchIds(classIds);
            classInfoMap = classes.stream().collect(Collectors.toMap(ClassInfo::getId, c -> c));

            // 4.2 提取 GradeId 并查询 GradeInfo
            List<Integer> gradeIds = classes.stream()
                    .map(ClassInfo::getGradeid)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (!gradeIds.isEmpty()) {
                List<GradeInfo> grades = gradeInfoMapper.selectBatchIds(gradeIds);
                gradeInfoMap = grades.stream().collect(Collectors.toMap(GradeInfo::getId, g -> g));
            }
        }

        // 4.3 查功能记录
        List<FuncItem> funcItems = funcItemMapper.selectList(new LambdaQueryWrapper<FuncItem>()
                .in(FuncItem::getUid, pageUids)
                .ge(FuncItem::getCreateTime, startTs)
                .le(FuncItem::getCreateTime, endTs));
        Map<Long, List<FuncItem>> funcMap = funcItems.stream().collect(Collectors.groupingBy(FuncItem::getUid));

        // 4.4 查学校作息 (查询该校所有配置，后续在内存中过滤)
        List<SchoolTime> allSchoolTimes = schoolTimeMapper.selectList(new LambdaQueryWrapper<SchoolTime>()
                .eq(SchoolTime::getSchoolid, query.getSchoolId())
                .orderByAsc(SchoolTime::getSort));

        // 4.5 学科名称字典
        List<Long> subjectIds = courseList.stream().map(CourseBegin::getXkid).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> subjectMap = Collections.emptyMap();
        if (!subjectIds.isEmpty()) {
            List<ChaptSetting> settings = chaptSettingMapper.selectList(new LambdaQueryWrapper<ChaptSetting>()
                    .eq(ChaptSetting::getSchoolid, query.getSchoolId())
                    .in(ChaptSetting::getSubjectid, subjectIds));
            subjectMap = settings.stream().collect(Collectors.toMap(ChaptSetting::getSubjectid, ChaptSetting::getName, (v1, v2) -> v1));
        }

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
            // 沿用 CourseBegin 中的班级名称
            vo.setClassName(course.getBjmc() != null ? course.getBjmc() : "");

            String subName = subjectMap.get(course.getXkid());
            if (subName == null) subName = course.getXkid() != null ? String.valueOf(course.getXkid()) : "";
            vo.setSubjectName(subName);

            // 处理时间
            LocalDateTime courseTime = LocalDateTime.ofEpochSecond(course.getCreateTime(), 0, ZoneOffset.of("+8"));
            vo.setDateStr(courseTime.toLocalDate().toString());

            // --- 核心修改：获取学段(xd) 和 季节(type) ---
            Integer xd = resolveXd(course.getClassid(), classInfoMap, gradeInfoMap);
            String seasonType = resolveSeasonType(courseTime.toLocalDate());

            // 匹配节次 (传入过滤参数)
            vo.setPeriodName(matchPeriodWithRules(courseTime.toLocalTime(), allSchoolTimes, xd, seasonType));

            // 统计功能
            List<FuncItem> ops = funcMap.getOrDefault(course.getUid(), Collections.emptyList());
            countFunctions(vo, course, ops);

            resultList.add(vo);
        }

        Page<TeacherFuncStatVO> resultPage = new Page<>();
        resultPage.setCurrent(coursePage.getCurrent());
        resultPage.setSize(coursePage.getSize());
        resultPage.setTotal(coursePage.getTotal());
        resultPage.setRecords(resultList);

        return resultPage;
    }

    // ---------------------------------------------------------
    // 辅助方法
    // ---------------------------------------------------------

    /**
     * 解析学段
     * 逻辑：CourseBegin(classid) -> ClassInfo(gradeid) -> GradeInfo(xd)
     */
    private Integer resolveXd(Long classId, Map<Long, ClassInfo> classMap, Map<Integer, GradeInfo> gradeMap) {
        if (classId == null) return null;
        ClassInfo classInfo = classMap.get(classId);
        if (classInfo == null || classInfo.getGradeid() == null) return null;

        GradeInfo gradeInfo = gradeMap.get(classInfo.getGradeid());
        if (gradeInfo == null || gradeInfo.getXd() == null) return null;

        // GradeInfo 中 xd 是 String (如 "21"), SchoolTime 中 xd 是 Integer
        try {
            return Integer.valueOf(gradeInfo.getXd());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析季节类型
     * 中国惯例：
     * Summer: 5月1日 - 9月30日 (劳动节到国庆节前)
     * Winter: 10月1日 - 4月30日
     */
    private String resolveSeasonType(LocalDate date) {
        int month = date.getMonthValue();
        // 5,6,7,8,9月 为夏季时间
        if (month >= 5 && month <= 9) {
            return "summer";
        } else {
            return "winter";
        }
    }

    /**
     * 增强版节次匹配
     * @param courseTime 上课时间
     * @param allTimes 该校所有时间表
     * @param xd 学段
     * @param type 季节
     */
    private String matchPeriodWithRules(LocalTime courseTime, List<SchoolTime> allTimes, Integer xd, String type) {
        if (allTimes == null || allTimes.isEmpty()) return "未知节次";

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        // 1. 过滤：只保留符合当前 学段 和 季节 的时间表
        List<SchoolTime> validTimes = allTimes.stream()
                .filter(t -> {
                    // 如果时间表中 xd 为空，默认匹配所有；如果不为空，必须匹配
                    boolean xdMatch = (t.getXd() == null) || (xd != null && t.getXd().equals(xd));
                    // 如果时间表中 type 为空，默认匹配所有；如果不为空，必须匹配
                    boolean typeMatch = (t.getType() == null) || (type != null && t.getType().equalsIgnoreCase(t.getType()));
                    // 这里通常作息表存的是 "winter"/"summer"，如果不区分需根据数据实际情况调整
                    // 如果数据库里 type 字段不区分 null，下面的逻辑更严谨：
                    if (t.getType() != null && !t.getType().isEmpty()) {
                        typeMatch = t.getType().equalsIgnoreCase(type);
                    } else {
                        typeMatch = true; // 通用时间表
                    }
                    return xdMatch && typeMatch;
                })
                .collect(Collectors.toList());

        // 2. 匹配时间范围
        for (SchoolTime t : validTimes) {
            try {
                // 有些数据可能存的是 "8:00"，有些是 "08:00"，LocalTime.parse 默认需要补零，
                // 建议数据库规范化，或者这里做简单的补零处理，这里假设数据格式标准 HH:mm
                LocalTime stdStart = LocalTime.parse(t.getStart(), timeFmt);
                LocalTime stdEnd = LocalTime.parse(t.getEnd(), timeFmt);

                // 宽松匹配：开始前15分钟也算这节课
                if (courseTime.isAfter(stdStart.minusMinutes(15)) && courseTime.isBefore(stdEnd)) {
                    return "第" + t.getSort() + "节";
                }
            } catch (Exception e) {
                // 忽略格式错误的数据
            }
        }
        return "其他时间";
    }

    private void countFunctions(TeacherFuncStatVO vo, CourseBegin course, List<FuncItem> ops) {
        // 原有统计逻辑保持不变
        long start = course.getCreateTime();
        long end = (course.getEndTime() == null) ? start + 2400 : Long.valueOf(course.getEndTime());
        if (end < start) {
            end = start + end; // 防御性处理
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