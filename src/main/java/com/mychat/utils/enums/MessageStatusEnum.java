package com.mychat.utils.enums;

import lombok.Getter;

/**
 * projectName: com.mychat.utils.enums
 * author:  SoulGoodman-coder
 * description: 消息状态枚举类
 */

@Getter
public enum MessageStatusEnum {
    SENDING(0, "发送中"),
    SENDED(1, "已发送");

    private Integer status;
    private String desc;

    private MessageStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static MessageStatusEnum getByStatus(Integer status) {
        for (MessageStatusEnum e : MessageStatusEnum.values()) {
            if (e.getStatus().equals(status)) {
                return e;
            }
        }
        return null;
    }
}
