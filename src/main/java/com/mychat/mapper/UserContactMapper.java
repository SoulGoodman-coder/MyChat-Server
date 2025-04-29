package com.mychat.mapper;

import com.mychat.entity.po.UserContact;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Administrator
* @description 针对表【user_contact(联系人表)】的数据库操作Mapper
* @createDate 2025-04-19 23:22:20
* @Entity com.mychat.entity.po.UserContact
*/
public interface UserContactMapper extends BaseMapper<UserContact> {
    List<UserContact> getGroupUserContactList(@Param("groupId") String groupId);
}




