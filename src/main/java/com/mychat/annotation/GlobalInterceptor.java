package com.mychat.annotation;

import java.lang.annotation.*;

/**
 * projectName: com.mychat.annotation
 * author:  SoulGoodman-coder
 * description: 前置拦截注解（未登录、admin拦截）
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GlobalInterceptor {
    // 校验登录
    boolean checkLogin() default true;
    // 校验管理员
    boolean checkAdmin() default false;
}
