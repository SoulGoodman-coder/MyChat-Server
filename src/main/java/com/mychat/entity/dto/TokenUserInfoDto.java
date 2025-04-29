package com.mychat.entity.dto;

import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;

/**
 * projectName: com.mychat.entity.dto
 * author:  SoulGoodman-coder
 * description:
 */

@Getter
@Setter
public class TokenUserInfoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String token;
    private String userId;
    private String nickName;

    // 标识是否是管理员用户
    private Boolean admin;
}
