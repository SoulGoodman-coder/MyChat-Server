package com.mychat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.mychat.mapper")
@EnableTransactionManagement    //开启事务
@EnableScheduling               //任务调度
@EnableAsync                    //异步
public class MyChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyChatApplication.class, args);
    }
}