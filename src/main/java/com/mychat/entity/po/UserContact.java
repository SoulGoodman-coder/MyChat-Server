package com.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * @TableName user_contact
 */
@TableName(value ="user_contact")
@Data
public class UserContact {
    private String userId;

    private String contactId;

    private Integer contactType;

    private Date createTime;

    private Integer status;

    private Date lastUpdateTime;
}