package com.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @TableName chat_session
 */
@TableName(value ="chat_session")
@Data
public class ChatSession implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private String sessionId;       // 会话ID

    private String lastMessage;     // 最后接收的消息

    private Long lastReceiveTime;   // 最后接收消息时间毫秒
}