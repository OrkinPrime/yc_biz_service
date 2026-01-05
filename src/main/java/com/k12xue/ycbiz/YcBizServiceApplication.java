package com.k12xue.ycbiz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@MapperScan("com.k12xue.ycbiz.mapper")
@SpringBootApplication
public class YcBizServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(YcBizServiceApplication.class, args);
    }

}
