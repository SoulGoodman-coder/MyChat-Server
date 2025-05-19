package com.mychat.mapper;

import com.mychat.entity.po.ChatSessionUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Administrator
* @description 针对表【chat_session_user(会话用户表)】的数据库操作Mapper
* @createDate 2025-05-14 00:00:01
* @Entity com.mychat.entity.po.ChatSessionUser
*/
public interface ChatSessionUserMapper extends BaseMapper<ChatSessionUser> {

    /**
     * 获取会话信息列表
     * @param userId    当前用户id
     * @return  List<ChatSessionUser>
     */
    List<ChatSessionUser> selectChatSessionInfoList(@Param("userId") String userId);
}




