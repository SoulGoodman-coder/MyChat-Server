package com.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mychat.utils.enums.UserContactTypeEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @TableName chat_session_user
 */
@TableName(value ="chat_session_user")
@Data
public class ChatSessionUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String userId;          // 用户ID

    private String contactId;       // 联系人ID

    private String sessionId;       // 会话ID

    private String contactName;     // 联系人名称

    @TableField(exist = false)
    private Integer contactType;    // 联系人类型 0：好友 1：群组

    @TableField(exist = false)
    private String lastMessage;     // 最后接收的消息

    @TableField(exist = false)
    private Long lastReceiveTime;   // 最后接收消息时间毫秒

    @TableField(exist = false)
    private Integer memberCount;    // 群聊人数（单聊则为0）

    public Integer getContactType() {
        return UserContactTypeEnum.getByPrefix(contactId).getType();
    }
}