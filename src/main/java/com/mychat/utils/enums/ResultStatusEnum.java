package com.mychat.utils.enums;

import lombok.Getter;

@Getter
public enum ResultStatusEnum {
    SUCCESS("success"),
    ERROR("error");

    private String status;

    ResultStatusEnum(String status) {
        this.status = status;
    }
}
