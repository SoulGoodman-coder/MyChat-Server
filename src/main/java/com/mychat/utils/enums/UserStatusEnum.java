package com.mychat.utils.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {
    DISABLE(0, "禁用"),
    ENABLE(1, "启用");

    private Integer statue;
    private String desc;

    UserStatusEnum(Integer statue, String desc) {
        this.statue = statue;
        this.desc = desc;
    }

    public static UserStatusEnum getByStatue(Integer statue) {
        for (UserStatusEnum item : UserStatusEnum.values()) {
            if (item.getStatue().equals(statue)){
                return item;
            }
        }
        return null;
    }
}
