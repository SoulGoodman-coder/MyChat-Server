package com.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @TableName chat_message
 */
@TableName(value ="chat_message")
@Data
public class ChatMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long messageId;             // 自增ID

    private String sessionId;           // 会话ID

    private Integer messageType;        // 消息类型

    private String messageContent;      // 消息内容

    private String sendUserId;          // 发送人ID

    private String sendUserNickName;    // 发送人昵称

    private Long sendTime;              // 发送时间

    private String contactId;           // 接收的联系人ID

    private Integer contactType;        // 联系人类型 0：好友 1：群组

    private Long fileSize;              // 文件大小

    private String fileName;            // 文件名

    private Integer fileType;           // 文件类型

    private Integer status;             // 状态 0:正在发送 1:已发送
}