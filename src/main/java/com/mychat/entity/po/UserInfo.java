package com.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import com.mychat.utils.enums.UserOnlineStatusEnum;
import lombok.Data;

/**
 * @TableName user_info
 */
@TableName(value ="user_info")
@Data
public class UserInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
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

    @TableField(exist = false)
    private Integer onlineType;

    public Integer getOnlineType() {
        if (null == lastOffTime){
            lastOffTime =  0L;
        }

        if (null != lastLoginTime && lastLoginTime.getTime() > lastOffTime) {
            return UserOnlineStatusEnum.ONLINE.getStatus();
        }
        return UserOnlineStatusEnum.OFFLINE.getStatus();
    }
}