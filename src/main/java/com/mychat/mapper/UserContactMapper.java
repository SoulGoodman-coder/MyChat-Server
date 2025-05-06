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
    // 获取群成员列表
    List<UserContact> getGroupUserContactList(@Param("groupId") String groupId);

    // 获取联系人（好友）
    List<UserContact> getUserContectList(@Param("userId") String userId);

    // 获取联系人（群组）（过滤自己创建的群组，只展示加入的群组）
    List<UserContact> getGroupContactList(@Param("userId") String userId);
}




