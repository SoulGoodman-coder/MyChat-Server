package com.mychat.controller;

import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.redis.RedisUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * projectName: com.mychat.controller
 * author:  SoulGoodman-coder
 * description: 所有controller的父类，放公共方法
 */

@Component("baseController")
public class BaseController {
    @Resource
    private RedisUtils redisUtils;

    @Value("${contants.REDIS_KEY_USER_TOKEN}")
    private String REDIS_KEY_USER_TOKEN;

    /**
     * 从请求头中获取token，封装到TokenUserInfoDto对象中
     * @param request   HttpServletRequest
     * @return          TokenUserInfoDto
     */
    protected TokenUserInfoDto getTokenUserInfoDto(HttpServletRequest request) {
        // 获取token
        String token = request.getHeader("token");
        return  (TokenUserInfoDto) redisUtils.get(REDIS_KEY_USER_TOKEN + token);
    }
}
