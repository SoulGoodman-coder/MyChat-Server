package com.mychat.controller;

import com.mychat.annotation.GlobalInterceptor;
import com.mychat.entity.config.AppConfig;
import com.mychat.entity.vo.UserInfoVo;
import com.mychat.exception.BusinessException;
import com.mychat.redis.RedisComponent;
import com.mychat.redis.RedisUtils;
import com.mychat.service.UserInfoService;
import com.mychat.utils.Result;
import com.wf.captcha.SpecCaptcha;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * projectName: com.mychat.controller
 * author:  SoulGoodman-coder
 * description: 登录注册controller
 */

@RestController("accountController")
@RequestMapping("account")
@Validated      // 参数校验
@Slf4j
public class AccountController {
    @Resource
    private RedisUtils redisUtils;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoService userInfoService;

    @Value("${contants.REDIS_KEY_CHECK_CODE}")
    private String REDIS_KEY_CHECK_CODE;

    @Value("${contants.TIME_SECOND_1MIN}")
    private Integer TIME_SECOND_1MIN;

    // 获取验证码
    @PostMapping("checkCode")
    public Result checkCode() {
        SpecCaptcha specCaptcha = new SpecCaptcha(100, 43, 4);
        String checkCode = specCaptcha.text();   // 验证码值
        String checkCodeBase64 = specCaptcha.toBase64();    // 验证码图片base64
        String checkCodeKey = UUID.randomUUID().toString();     // 为当前验证码生成一个uuid用作标识

        // 将验证码放入redis
        redisUtils.set(REDIS_KEY_CHECK_CODE + checkCodeKey, checkCode, TIME_SECOND_1MIN * 10);

        // 封装返回数据
        Map<String, String> map = new HashMap<>();
        map.put("checkCode", checkCodeBase64);
        map.put("checkCodeKey", checkCodeKey);
        return Result.ok(map);
    }

    // 注册
    @PostMapping("register")
    public Result register(@NotBlank @Email String email,
                           @NotBlank String nickName,
                           @NotBlank @Pattern(regexp = AppConfig.REGEX_PASSWORD) String password,
                           @NotBlank String checkCodeKey,
                           @NotBlank String checkCode) {
        try {
            // 验证码不匹配
            if (! checkCode.equalsIgnoreCase( (String) redisUtils.get(REDIS_KEY_CHECK_CODE + checkCodeKey))){
                throw new BusinessException("图片验证码不正确");
            }
            userInfoService.register(email, nickName, password);
            return Result.ok("注册成功");
        } finally {
            redisUtils.delete(REDIS_KEY_CHECK_CODE + checkCodeKey);
        }
    }

    // 登录
    @PostMapping("login")
    public Result login(@NotBlank @Email String email,
                           @NotBlank String password,
                           @NotBlank String checkCodeKey,
                           @NotBlank String checkCode) {
        try {
            // 验证码不匹配
            if (! checkCode.equalsIgnoreCase( (String) redisUtils.get(REDIS_KEY_CHECK_CODE + checkCodeKey))){
                throw new BusinessException("图片验证码不正确");
            }
            // 登录 返回登录信息给前端
            UserInfoVo userInfoVo = userInfoService.login(email, password);
            return Result.ok(userInfoVo);
        } finally {
            redisUtils.delete(REDIS_KEY_CHECK_CODE + checkCodeKey);
        }
    }

    // 获取系统的设置
    @PostMapping("getSysSetting")
    @GlobalInterceptor
    public Result getSysSetting() {
       return Result.ok(redisComponent.getSysSettingDto());
    }
}
