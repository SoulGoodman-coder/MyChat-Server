package com.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * @TableName user_info
 */
@TableName(value ="user_info")
@Data
public class UserInfo {
    private String userId;

    private String email;

    private String nickName;

    private Integer joinType;

    private Integer sex;

    private String password;

    private String personalSignature;

    private Integer status;

    private Date createTime;

    private Date lastLoginTime;

    private String areaName;

    private String areaCode;

    private Long lastOffTime;
}