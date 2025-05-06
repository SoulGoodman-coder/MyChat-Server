package com.mychat.entity.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * projectName: com.mychat.entity.vo
 * author:  SoulGoodman-coder
 * description: 当前登录的用户信息
 */

@Getter
@Setter
public class UserInfoVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String userId;

    private String nickName;

    private Integer joinType;

    private Integer sex;

    private String personalSignature;

    private String areaName;

    private String areaCode;

    private String token;

    private Boolean admin;

    // 与联系人的状态  0:非好友 1:好友 2:已删除好友 3:被好友删除 4:已拉黑好友 5:加好友时被好友拉黑 6:加好友后被好友拉黑
    private Integer contactStatus;
}
