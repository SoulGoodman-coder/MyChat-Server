package com.mychat.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mychat.entity.po.UserInfoBeauty;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author Administrator
* @description 针对表【user_info_beauty(靓号表)】的数据库操作Mapper
* @createDate 2025-04-01 22:37:43
* @Entity com.mychat.pojo.UserInfoBeauty
*/
public interface UserInfoBeautyMapper extends BaseMapper<UserInfoBeauty> {

    /**
     * 获取靓号列表
     * @param page          IPage接口实现对象
     * @param userIdFuzzy   靓号（支持模糊搜索）
     * @param emailFuzzy    邮箱（支持模糊搜索）
     * @return IPage<UserInfoBeauty>
     */
    IPage<UserInfoBeauty> loadBeautyAccountList(Page<UserInfoBeauty> page, @Param("userIdFuzzy") String userIdFuzzy, @Param("emailFuzzy") String emailFuzzy);
}




