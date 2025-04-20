package com.mychat.utils.enums;

import com.mychat.utils.StringUtils;
import lombok.Getter;

// 添加好友方式枚举
@Getter
public enum JoinTypeEnum {
    JOIN(0, "直接加入"),
    APPLY(1, "需要审核");

    private Integer type;
    private String desc;

    JoinTypeEnum(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static JoinTypeEnum getByName(String name){
        try {
            if(StringUtils.isEmpty(name)){
                return null;
            }
            return JoinTypeEnum.valueOf(name.toUpperCase());
        }catch (IllegalArgumentException e){
            return null;
        }
    }

    public static JoinTypeEnum getByType(Integer joinType){
        for(JoinTypeEnum joinTypeEnum : JoinTypeEnum.values()){
            if(joinTypeEnum.getType().equals(joinType)){
                return joinTypeEnum;
            }
        }
        return null;
    }
}
