package com.k12xue.ycbiz;

import com.k12xue.ycbiz.mapper.CourseBeginMapper;
import com.k12xue.ycbiz.mapper.SchoolTimeMapper;
import com.k12xue.ycbiz.model.entity.CourseBegin;
import com.k12xue.ycbiz.model.entity.SchoolTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


/**
 * <p>
 * 数据库测试
 * </p>
 *
 * <p>测试本项目对数据库k12和operation_db的联通性，结果反映在控制台</p>
 *
 * @author 李忠斌
 * @since 2026-01-05
 */
@SpringBootTest
public class DataTest {

    @Autowired
    private SchoolTimeMapper schoolTimeMapper;
    @Autowired
    private CourseBeginMapper courseBeginMapper;

    @Test
    void test() {
        System.out.println("========== 开始测试主库 (operation_db) ==========");
        // selectList(null) 相当于 SELECT * FROM operation_db.users
        List<SchoolTime> users = schoolTimeMapper.selectList(null);
        System.out.println("主库查询成功，记录数：" + users.size());
        if (!users.isEmpty()) {
            System.out.println("第一条数据：" + users.get(0));
        }

        System.out.println("\n========== 开始测试副库 (k12) ==========");
        // selectList(null) 相当于 SELECT * FROM k12.student
        // 因为我们在实体类 @TableName 里写了 "k12.student"
        List<CourseBegin> students = courseBeginMapper.selectList(null);
        System.out.println("K12库查询成功，记录数：" + students.size());
        if (!students.isEmpty()) {
            System.out.println("第一条数据：" + students.get(0));
        }
    }
}
