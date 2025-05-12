package com.mychat.service;

import com.mychat.entity.po.UserInfoBeauty;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Administrator
* @description 针对表【user_info_beauty(靓号表)】的数据库操作Service
* @createDate 2025-04-01 22:37:43
*/
public interface UserInfoBeautyService extends IService<UserInfoBeauty> {

    /**
     * 保存靓号
     * @param userInfoBeauty    靓号信息对象
     */
    void saveBeautAccount(UserInfoBeauty userInfoBeauty);

    /**
     * 获取靓号列表
     * @param userIdFuzzy   靓号（支持模糊搜索）
     * @param emailFuzzy    邮箱（支持模糊搜索）
     * @param pageNumber    页码
     * @param pageSize      页容量
     * @return List<UserInfoBeauty>
     */
    List<UserInfoBeauty> loadBeautyAccountList(String userIdFuzzy, String emailFuzzy, Integer pageNumber, Integer pageSize);
}
