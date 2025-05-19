package com.mychat.utils.enums;

import lombok.Getter;

/**
 * projectName: com.mychat.utils.enums
 * author:  SoulGoodman-coder
 * description: 群组添加或移除人员操作类型
 */

@Getter
public enum GroupUserOpTypeEnum {
    ADD(0, "添加人员"),
    REMOVE(1, "移除人员");

    private Integer type;
    private String desc;

    GroupUserOpTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static GroupUserOpTypeEnum getByType(Integer type) {
        for (GroupUserOpTypeEnum groupUserOpType: GroupUserOpTypeEnum.values()) {
            if (groupUserOpType.getType().equals(type)){
                return groupUserOpType;
            }
        }
        return null;
    }
}
