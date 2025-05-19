package com.mychat;

import com.mychat.redis.RedisUtils;
import com.mychat.websocket.netty.NettyWebSocketStart;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.Executor;

/**
 * projectName: com.mychat
 * author:  SoulGoodman-coder
 * description: 初始化
 */

@Component("initRun")
@Slf4j
public class InitRun implements ApplicationRunner, AsyncConfigurer {
    @Resource
    private DataSource dataSource;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private NettyWebSocketStart nettyWebSocketStart;

    @Override
    public void run(ApplicationArguments args) {
        try {
            dataSource.getConnection();
            redisUtils.get("test");
            nettyWebSocketStart.startNettyService();

            log.info("SpringBoot服务启动成功，数据库已连接，netty已启动");
        }catch (SQLException e) {
            log.error("Mysql数据库连接失败，请检查配置");
        }catch (RedisConnectionFailureException e){
            log.error("Redis数据库连接失败，请检查配置");
        }catch (Exception e){
            log.error("服务启动失败", e);
        }
    }

    //注册执行器
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setMaxPoolSize(80);
        taskExecutor.setQueueCapacity(100);
        taskExecutor.initialize();//如果不初始化，导致找到不到执行器
        return taskExecutor;
    }

    // 用于捕获异步异常
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }

}
