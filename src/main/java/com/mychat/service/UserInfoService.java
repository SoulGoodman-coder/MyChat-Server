package com.mychat.service;

import com.mychat.entity.po.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mychat.entity.vo.UserInfoVo;

import java.util.Map;

/**
* @author Administrator
* @description 针对表【user_info(用户信息表)】的数据库操作Service
* @createDate 2025-04-01 22:37:22
*/
public interface UserInfoService extends IService<UserInfo> {

    /**
     * 用户注册
     * @param email         邮箱
     * @param nickName      昵称
     * @param password      密码
     */
    void register(String email, String nickName, String password);

    /**
     * 用户登录
     * @param email         邮箱
     * @param password      密码
     */
    UserInfoVo login(String email, String password);
}
