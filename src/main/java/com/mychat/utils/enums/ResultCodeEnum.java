package com.mychat.utils.enums;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {
    SUCCESS(200, "请求成功"),
    CODE_404(404, "请求地址不存在"),
    CODE_500(500, "服务器返回错误，请联系管理员"),
    CODE_600(600, "请求参数错误"),
    CODE_601(601, "信息已存在"),
    CODE_602(602, "文件不存在"),
    CODE_901(901, "登录超时"),
    CODE_902(902, "您还不是对方的好友，请先添加好友"),
    CODE_903(903, "您已经不在群聊中，请先加入群聊");

    private Integer code;
    private String msg;

    ResultCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
