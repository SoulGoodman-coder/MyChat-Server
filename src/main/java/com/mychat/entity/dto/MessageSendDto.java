package com.mychat.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mychat.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * projectName: com.mychat.entity.dto
 * author:  SoulGoodman-coder
 * description: 封装发送的ws消息实体类
 */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)     // 序列化时忽略不存在的字段
public class MessageSendDto<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long messageId;     // 消息id

    private String sessionId;   // 会话id

    private String sendUserId;  // 发送人id

    private String sendUserNickName;    // 发送人昵称

    private String contactId;   // 联系人id

    private String contactName; // 联系人名称

    private String messageContent;      // 消息内容
    
    private String lastMessage; // 最后一条消息
    
    private Integer messageType;        // 消息类型

    private Long sendTime;      // 发送时间

    private Integer contactType;        // 联系人类型 0：单聊 1：群聊

    private T extendDate;       // 扩展消息

    private Integer status;     // 消息状态 0：发送中 1：已发送

    private Long fileSize;      // 文件大小

    private String fileName;    // 文件名

    private Integer fileType;   // 文件类型

    private Integer memberCount;        // 群聊人数（单聊则为0）

    public String getLastMessage() {
        if (StringUtils.isEmpty(lastMessage)){
            return messageContent;
        }
        return lastMessage;
    }
}
