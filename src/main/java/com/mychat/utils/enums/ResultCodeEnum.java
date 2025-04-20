package com.mychat.utils.enums;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {
    SUCCESS(200, "请求成功"),
    CODE_404(404, "请求地址不存在"),
    CODE_500(500, "服务器返回错误，请联系管理员"),
    CODE_600(600, "请求参数错误"),
    CODE_601(601, "信息已存在");

    private Integer code;
    private String msg;

    ResultCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
