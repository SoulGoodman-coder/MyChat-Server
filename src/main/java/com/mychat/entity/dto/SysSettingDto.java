package com.mychat.entity.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;

/**
 * projectName: com.mychat.entity.dto
 * author:  SoulGoodman-coder
 * description: 系统设置实体类
 */
@Component
@Getter
@Setter
public class SysSettingDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // 最多可创建群组数
    private Integer maxGroupCount = 5;
    // 群组最大成员数
    private Integer maxGroupMemberCount = 500;
    // 图片大小（MB）
    private Integer maxImageSize = 10;
    // 视频大小（MB）
    private Integer maxVideoSize = 100;
    // 其他文件大小（MB）
    private Integer maxFileSize = 10;
    // 机器人uid
    @Value("${contants.ROBOT_UID}")
    private String robotUid;
    // 机器人昵称
    private String robotNickName = "MyChat";
    // 机器人欢迎消息
    private String robotWelcome = "欢迎使用MyChat";
}
