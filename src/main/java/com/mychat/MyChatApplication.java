package com.mychat;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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

    // mybatis-plus插件配置
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        // 所有mybatis-plus插件的集合，所有需要的插件，都加入到该集合
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 加入分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}