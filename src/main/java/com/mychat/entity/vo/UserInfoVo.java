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

    // TODO contactStatus
    private Integer contactStatus;
}
