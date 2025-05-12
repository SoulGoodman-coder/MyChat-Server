package com.mychat.utils.enums;

import lombok.Getter;

/**
 * projectName: com.mychat.utils.enums
 * author:  SoulGoodman-coder
 * description: 版本更新文件类型枚举类
 */

@Getter
public enum AppUpdateFileTypeEnum {
    LOCAL(0, "本地"),
    OUTER_LINK(1, "外链");

    private Integer type;
    private String desc;


    AppUpdateFileTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static AppUpdateFileTypeEnum getByType(Integer type) {
        for (AppUpdateFileTypeEnum appUpdateFileTypeEnum : AppUpdateFileTypeEnum.values()) {
            if (appUpdateFileTypeEnum.getType().equals(type)) {
                return appUpdateFileTypeEnum;
            }
        }
        return null;
    }
}
