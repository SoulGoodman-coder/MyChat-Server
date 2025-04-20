package com.mychat.aspect;

import com.mychat.annotation.GlobalInterceptor;
import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.exception.BusinessException;
import com.mychat.redis.RedisUtils;
import com.mychat.utils.StringUtils;
import com.mychat.utils.enums.ResultCodeEnum;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * projectName: com.mychat.aspect
 * author:  SoulGoodman-coder
 * description: 全局切面类
 */

@Aspect
@Component("globalOperationAspect")
@Slf4j
public class GlobalOperationAspect {
    @Resource
    private RedisUtils redisUtils;

    @Resource
    private HttpServletRequest request;

    @Value("${contants.REDIS_KEY_USER_TOKEN}")
    private String REDIS_KEY_USER_TOKEN;

    @Before("@annotation(com.mychat.annotation.GlobalInterceptor)")
    public void interceptorDo(JoinPoint joinPoint){
        try {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
            if (interceptor != null) {
                if (interceptor.checkLogin() || interceptor.checkAdmin()){
                    checkLogin(interceptor.checkAdmin());
                }
            }
        }catch (BusinessException e){
            log.error("全局拦截异常", e);
            throw e;
        } catch (Throwable e){
            log.error("全局拦截异常", e);
            throw new BusinessException(ResultCodeEnum.CODE_500);
        }

    }

    private void checkLogin(boolean checkAdmin){
        // 获取token
        String token = request.getHeader("token");

        if (StringUtils.isEmpty(token)){
            throw new BusinessException(ResultCodeEnum.CODE_901);
        }

        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(REDIS_KEY_USER_TOKEN + token);

        if (null == tokenUserInfoDto){
            throw new BusinessException(ResultCodeEnum.CODE_901);
        }

        // 判断admin
        if (checkAdmin && !tokenUserInfoDto.getAdmin()){
            throw new BusinessException(ResultCodeEnum.CODE_404);
        }
    }

}
