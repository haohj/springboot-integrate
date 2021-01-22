package com.hao.shardingjdbc;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hao.shardingjdbc.entity.Course;
import com.hao.shardingjdbc.mapper.CourseMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ShardingjdbcApplicationTests {
    @Autowired
    private CourseMapper courseMapper;

    //垂直分表

    //垂直分库

    //水平分表
    @Test
    public void addCourse() {
        for (int i = 1; i <= 10; i++) {
            Course course = new Course();
            course.setCname("java" + i);
            course.setUserId(100L);
            course.setCstatus("Normal" + i);
            courseMapper.insert(course);
        }
    }

    @Test
    public void findCourse() {
        QueryWrapper<Course> wrapper = new QueryWrapper<>();
        wrapper.eq("cid", 1L);
        Course course = courseMapper.selectOne(wrapper);
        System.out.println(course);
    }

    //水平分库

}
