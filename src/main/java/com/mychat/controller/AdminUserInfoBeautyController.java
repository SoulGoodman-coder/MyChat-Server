package com.mychat.controller;

import com.mychat.annotation.GlobalInterceptor;
import com.mychat.entity.po.UserInfoBeauty;
import com.mychat.service.UserInfoBeautyService;
import com.mychat.utils.Result;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * projectName: com.mychat.controller
 * author:  SoulGoodman-coder
 * description: 靓号后台管理相关controller
 */

@RestController("adminUserInfoBeautyController")
@RequestMapping("admin")
@Validated      // 参数校验
public class AdminUserInfoBeautyController extends BaseController{
    @Resource
    private UserInfoBeautyService userInfoBeautyService;

    /**
     * 获取靓号列表
     * @param userIdFuzzy   靓号（支持模糊搜索）
     * @param emailFuzzy    邮箱（支持模糊搜索）
     * @param pageNumber    页码
     * @param pageSize      页容量
     * @return Result
     */
    @PostMapping("loadBeautyAccountList")
    @GlobalInterceptor(checkAdmin = true)
    public Result loadBeautyAccountList(String userIdFuzzy, String emailFuzzy, Integer pageNumber, Integer pageSize){

        List<UserInfoBeauty> userInfoList = userInfoBeautyService.loadBeautyAccountList(userIdFuzzy, emailFuzzy, pageNumber, pageSize);

        return Result.ok(userInfoList);
    }

    /**
     * 保存靓号
     * @param userInfoBeauty    靓号信息对象
     * @return Result
     */
    @PostMapping("saveBeautAccount")
    @GlobalInterceptor(checkAdmin = true)
    public Result saveBeautAccount(UserInfoBeauty userInfoBeauty){

        userInfoBeautyService.saveBeautAccount(userInfoBeauty);

        return Result.ok(null);
    }

    /**
     * 删除靓号
     * @param id                靓号id
     * @return Result
     */
    @PostMapping("delBeautAccount")
    @GlobalInterceptor(checkAdmin = true)
    public Result delBeautAccount(@NotNull Integer id){

        userInfoBeautyService.removeById(id);

        return Result.ok(null);
    }
}
