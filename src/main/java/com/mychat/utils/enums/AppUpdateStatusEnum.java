package com.mychat.utils.enums;

import lombok.Getter;

/**
 * projectName: com.mychat.utils.enums
 * author:  SoulGoodman-coder
 * description: 版本更新发布状态枚举
 */

@Getter
public enum AppUpdateStatusEnum {
    INIT(0, "未发布"),
    GRAYSCALE(1, "灰度发布"),
    ALL(2, "全网发布");

    private Integer status;
    private String desc;


    AppUpdateStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static AppUpdateStatusEnum getByStatus(Integer status) {
        for (AppUpdateStatusEnum e : AppUpdateStatusEnum.values()) {
            if (e.status.equals(status)) {
                return e;
            }
        }
        return null;
    }
}
