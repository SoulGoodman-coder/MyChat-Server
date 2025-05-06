package com.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mychat.utils.enums.UserContactApplyStatusEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

/**
 * @TableName user_contact_apply
 */
@TableName(value ="user_contact_apply")
@Data
public class UserContactApply {
    @TableId
    private Integer applyId;

    private String applyUserId;

    private String receiveUserId;

    private Integer contactType;

    private String contactId;

    private Long lastApplyTime;

    private Integer status;

    private String applyInfo;

    @TableField(exist = false)
    private String contactName; // 申请对象名（用户名或群聊名）

    @TableField(exist = false)
    @Getter(AccessLevel.NONE)
    private String statusName;  // 申请状态名（待处理、已同意、已拒绝、已拉黑）

    public String getStatusName() {
        UserContactApplyStatusEnum statusEnum = UserContactApplyStatusEnum.getByStatus(status);
        return statusEnum == null ? null : statusEnum.getDesc();
    }
}