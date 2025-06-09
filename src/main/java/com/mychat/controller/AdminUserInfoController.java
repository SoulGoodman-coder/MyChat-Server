package com.mychat.controller;

import com.mychat.annotation.GlobalInterceptor;
import com.mychat.entity.po.UserInfo;
import com.mychat.service.UserInfoService;
import com.mychat.utils.Result;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * projectName: com.mychat.controller
 * author:  SoulGoodman-coder
 * description: 管理后台相关controller
 */

@RestController("adminUserInfoController")
@RequestMapping("admin")
@Validated      // 参数校验
public class AdminUserInfoController {

    @Resource
    private UserInfoService userInfoService;

    /**
     * 获取用户列表
     * @param userId        用户id
     * @param nickNameFuzzy 用户昵称（支持模糊搜索）
     * @param pageNumber    页码
     * @param pageSize      页容量
     * @return Result
     */
    @PostMapping("loadUser")
    @GlobalInterceptor(checkAdmin = true)
    public Result loadUser(String userId, String nickNameFuzzy, Integer pageNumber, Integer pageSize){

        Map<String, Object> userInfoMap = userInfoService.loadUser(userId, nickNameFuzzy, pageNumber, pageSize);

        return Result.ok(userInfoMap);
    }

    /**
     * 更新用户状态
     * @param status        新的用户状态 0：禁用  1：启用
     * @param userId        目标用户id
     * @return Result
     */
    @PostMapping("updateUserStatus")
    @GlobalInterceptor(checkAdmin = true)
    public Result updateUserStatus(@NotNull Integer status, @NotBlank String userId){

        userInfoService.updateUserStatus(status, userId);

        return Result.ok(null);
    }

    /**
     * 强制下线
     * @param userId        被强制下线的用户id
     * @return Result
     */
    @PostMapping("forceOffLine")
    @GlobalInterceptor(checkAdmin = true)
    public Result forceOffLine(@NotBlank String userId){

        userInfoService.forceOffLine(userId);

        return Result.ok(null);
    }

}
