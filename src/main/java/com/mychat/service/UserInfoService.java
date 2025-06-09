package com.mychat.service;

import com.mychat.entity.po.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mychat.entity.vo.UserInfoVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
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

    /**
     * 修改用户信息
     * @param userInfo      修改后的用户信息对象
     * @param avatarFile    原用户头像
     * @param avatarCover   用户头像缩略图
     */
    void updateUserInfo(UserInfo userInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException;

    /**
     * 获取用户列表
     * @param userId        用户id
     * @param nickNameFuzzy 用户昵称（支持模糊搜索）
     * @param pageNumber    页码
     * @param pageSize      页容量
     * @return Map<String, Object>
     */
    Map<String, Object> loadUser(String userId, String nickNameFuzzy, Integer pageNumber, Integer pageSize);

    /**
     * 更新用户状态
     * @param status        新的用户状态 0：禁用  1：启用
     * @param userId        目标用户id
     */
    void updateUserStatus(Integer status, String userId);

    /**
     * 强制下线
     * @param userId        被强制下线的用户id
     */
    void forceOffLine(String userId);
}
