package com.mychat.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mychat.entity.po.UserContactApply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author Administrator
* @description 针对表【user_contact_apply(好友申请表)】的数据库操作Mapper
* @createDate 2025-04-19 23:22:28
* @Entity com.mychat.entity.po.UserContactApply
*/
public interface UserContactApplyMapper extends BaseMapper<UserContactApply> {

    IPage<UserContactApply> loadApply(IPage<UserContactApply> page, @Param("receiveUserId") String receiveUserId);
}




