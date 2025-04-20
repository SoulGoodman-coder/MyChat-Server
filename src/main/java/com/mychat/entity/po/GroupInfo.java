package com.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * @TableName group_info
 */
@TableName(value ="group_info")
@Data
public class GroupInfo {
    private String groupId;

    private String groupName;

    private String groupOwnerId;

    private Date createTime;

    private String groupNotice;

    private Integer joinType;

    private Integer status;
}