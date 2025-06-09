package com.mychat.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mychat.entity.po.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author Administrator
* @description 针对表【user_info(用户信息表)】的数据库操作Mapper
* @createDate 2025-04-01 22:37:22
* @Entity com.mychat.pojo.UserInfo
*/
public interface UserInfoMapper extends BaseMapper<UserInfo> {

    /**
     * 获取用户列表
     * @param page          IPage接口实现对象
     * @param userId        用户id
     * @param nickNameFuzzy 用户昵称（支持模糊搜索）
     * @return IPage<UserInfo>
     */
    IPage<UserInfo> loadUser(Page<UserInfo> page, @Param("userId") String userId, @Param("nickNameFuzzy") String nickNameFuzzy);
}




