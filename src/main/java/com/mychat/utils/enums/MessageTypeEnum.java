package com.mychat.utils.enums;

import lombok.Getter;

/**
 * projectName: com.mychat.utils.enums
 * author:  SoulGoodman-coder
 * description: ws发送消息类型
 */

@Getter
public enum MessageTypeEnum {
    INIT(0, "", "连接ws获取消息"),
    ADD_FRIEND(1, "", "添加好友打招呼消息"),
    CHAT(2, "", "普通聊天消息"),
    GROUP_CREATE(3, "群聊已经创建好了，可以和好友一起畅聊了", "创建群聊消息"),
    CONTACT_APPLY(4, "", "好友申请消息"),
    MEDIA_CHAT(5, "", "媒体文件消息"),
    FILE_UPLOAD(6, "", "文件上传完成消息"),
    FORCE_OFF_LINE(7, "", "强制下线消息"),
    DISSOLUTION_GROUP(8, "群聊已解散", "解散群聊消息"),
    ADD_GROUP(9, "%s加入群聊", "加入群聊消息"),
    CONTACT_NAME_UPDATE(10, "", "更新用户昵称或群名称消息"),
    LEAVE_GROUP(11, "%s退出群聊", "退出群聊消息"),
    REMOVE_GROUP(12, "%s被管理员移出了群聊", "被管理员移出群聊消息"),
    ADD_FRIEND_SELF(13, "", "添加好友打招呼消息");


    private Integer type;
    private String initMessage;
    private String desc;

    MessageTypeEnum(Integer type, String initMessage, String desc) {
        this.type = type;
        this.initMessage = initMessage;
        this.desc = desc;
    }

    public static MessageTypeEnum getByType(Integer type) {
        for (MessageTypeEnum m : MessageTypeEnum.values()) {
            if (m.getType().equals(type)) {
                return m;
            }
        }
        return null;
    }
}
