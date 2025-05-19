package com.mychat.utils.enums;

import lombok.Getter;

/**
 * projectName: com.mychat.utils.enums
 * author:  SoulGoodman-coder
 * description: 用户在线状态
 */

@Getter
public enum UserOnlineStatusEnum {
    OFFLINE(0, "离线"),
    ONLINE(1, "在线");

    private Integer status;
    private String desc;

    UserOnlineStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static UserOnlineStatusEnum getByStatus(Integer status) {
        for (UserOnlineStatusEnum e : UserOnlineStatusEnum.values()) {
            if (e.getStatus().equals(status)) {
                return e;
            }
        }
        return null;
    }
}
