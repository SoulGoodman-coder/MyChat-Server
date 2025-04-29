package com.mychat;

import com.mychat.redis.RedisUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * projectName: com.mychat
 * author:  SoulGoodman-coder
 * description: 初始化
 */

@Component("initRun")
@Slf4j
public class InitRun implements ApplicationRunner {
    @Resource
    private DataSource dataSource;

    @Resource
    private RedisUtils redisUtils;

    @Override
    public void run(ApplicationArguments args) {
        try {
            dataSource.getConnection();
            redisUtils.get("test");
            log.info("服务启动成功，数据库已连接");
        }catch (SQLException e) {
            log.error("Mysql数据库连接失败，请检查配置");
        }catch (RedisConnectionFailureException e){
            log.error("Redis数据库连接失败，请检查配置");
        }catch (Exception e){
            log.error("服务启动失败", e);
        }
    }
}
