package com.mychat.service;

import com.mychat.entity.po.ChatSessionUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Administrator
* @description 针对表【chat_session_user(会话用户表)】的数据库操作Service
* @createDate 2025-05-14 00:00:01
*/
public interface ChatSessionUserService extends IService<ChatSessionUser> {
    /**
     * 根据userId和contactId 插入或修改 applyChatSessionUser表
     * @param chatSessionUser      会话用户信息对象
     */
    void insertOrUpdateChatSessionUser(ChatSessionUser chatSessionUser);

    /**
     * 根据contactId更新contactName
     * @param contactId             修改名称的用户id或群组id
     * @param contactName           修改后的名称
     */
    void updateContactNameByContactId(String contactId, String contactName);
}
