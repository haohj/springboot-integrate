package com.hao.shardingjdbc.entity;

import lombok.Data;

@Data
public class Course {
    private Long cid;
    private String name;
    private Long userId;
    private String cstatus;
}
