package com.mychat.entity.dto;

import com.mychat.entity.po.ChatMessage;
import com.mychat.entity.po.ChatSessionUser;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * projectName: com.mychat.entity.dto
 * author:  SoulGoodman-coder
 * description: 用户连接时，ws给用户返回数据的实体类
 */

@Getter
@Setter
public class WsInitDateDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<ChatSessionUser> chatSessionUserList;      // 会话用户列表

    private List<ChatMessage> chatMessageList;              // 聊天信息列表

    private Integer applyCount;                             // 好友申请消息数

}
