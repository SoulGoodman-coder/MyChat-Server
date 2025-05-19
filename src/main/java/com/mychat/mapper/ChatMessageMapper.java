package com.mychat.mapper;

import com.mychat.entity.po.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Administrator
* @description 针对表【chat_message(聊天消息表)】的数据库操作Mapper
* @createDate 2025-05-14 00:00:01
* @Entity com.mychat.entity.po.ChatMessage
*/
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 根据联系人id列表查询聊天消息
     * @param contactIdList     联系人id列表
     * @param lastOffTime       最后离线时间（只查询离线后的消息）
     * @return List<ChatMessage>
     */
    List<ChatMessage> selectChatMessageByContactIdList(@Param("contactIdList") List<String> contactIdList, @Param("lastOffTime") Long lastOffTime);
}




