package com.mychat.utils.enums;

import lombok.Getter;

@Getter
public enum BeautyAccountStatusEnum {
    NO_USE(0, "未使用"),
    USED(1, "已使用");

    private Integer status;
    private String desc;

    BeautyAccountStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public BeautyAccountStatusEnum getByStatus(Integer status) {
        for (BeautyAccountStatusEnum item : BeautyAccountStatusEnum.values()) {
            if (item.getStatus().equals(status)){
                return item;
            }
        }
        return null;
    }
}
