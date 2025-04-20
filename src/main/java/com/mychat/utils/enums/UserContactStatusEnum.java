package com.mychat.utils.enums;

import com.mychat.utils.StringUtils;
import lombok.Getter;

import java.util.Objects;

/**
 * projectName: com.mychat.utils.enums
 * author:  SoulGoodman-coder
 * description: 联系人状态
 */

@Getter
public enum UserContactStatusEnum {
    // 状态 0:非好友 1:好友 2:已删除好友 3:被好友删除 4:已拉黑好友 5:被好友拉黑
    NOT_FRIEND(0, "非好友"),
    FRIEND(1, "好友"),
    DEL(2, "已删除好友"),
    DEL_BE(3, "被好友删除"),
    BLACKLIST(4, "已拉黑好友"),
    BLACKLIST_BE(5, "被好友拉黑");

    private Integer status;
    private String desc;

    UserContactStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static UserContactStatusEnum getByName(String name) {
        try {
            if (StringUtils.isEmpty(name)){
                return null;
            }
            return UserContactStatusEnum.valueOf(name.toUpperCase());
        }catch (IllegalArgumentException e){
            return null;
        }
    }

    public static UserContactStatusEnum getByStatus(Integer status) {
        for (UserContactStatusEnum userContactStatusEnum : UserContactStatusEnum.values()) {
            if (userContactStatusEnum.getStatus().equals(status)) {
                return userContactStatusEnum;
            }
        }
        return null;
    }
}
