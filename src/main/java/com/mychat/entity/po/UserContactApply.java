package com.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @TableName user_contact_apply
 */
@TableName(value ="user_contact_apply")
@Data
public class UserContactApply {
    private Integer applyId;

    private String applyUserId;

    private String receiveUserId;

    private Integer contactType;

    private String contactId;

    private Long lastApplyTime;

    private Integer status;

    private String applyInfo;
}