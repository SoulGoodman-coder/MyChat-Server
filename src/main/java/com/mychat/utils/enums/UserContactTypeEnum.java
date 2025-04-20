package com.mychat.utils.enums;

import com.mychat.utils.StringUtils;
import lombok.Getter;

@Getter
public enum UserContactTypeEnum {
    USER(0, "U", "好友"),
    GROUP(1, "G", "群");

    private Integer type;
    private String prefix;
    private String desc;

    UserContactTypeEnum(Integer type, String prefix, String desc) {
        this.type = type;
        this.prefix = prefix;
        this.desc = desc;
    }

    /**
     * 根据name获取UserTypeEnum
     * @param name      name
     * @return          UserTypeEnum
     */
    public static UserContactTypeEnum getByName(String name) {
        try {
            if(StringUtils.isEmpty(name)){
                return null;
            }
            return UserContactTypeEnum.valueOf(name.toUpperCase());
        }catch (Exception e){
            return null;
        }
    }

    /**
     * 根据id获取UserTypeEnum
     * @param id    id
     * @return      UserTypeEnum
     */
    public static UserContactTypeEnum getByPrefix(String id) {
        try {
            if(StringUtils.isEmpty(id)){
                return null;
            }
            // 从id中解析标识
            String prefix = id.substring(0, 1);
            for (UserContactTypeEnum typeEnum : UserContactTypeEnum.values()) {
                if (typeEnum.prefix.equals(prefix)) {
                    return typeEnum;
                }
            }
            return null;
        }catch (Exception e){
            return null;
        }
    }
}
