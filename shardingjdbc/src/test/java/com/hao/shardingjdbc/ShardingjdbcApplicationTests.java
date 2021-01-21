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

    @Test
    void contextLoads() {
        QueryWrapper<Course> wrapper = new QueryWrapper<>();
        wrapper.eq("cid", 1L);
        courseMapper.selectOne(wrapper);
    }

}
