package com.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName group_info
 */
@TableName(value ="group_info")
@Data
public class GroupInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private String groupId;

    private String groupName;

    private String groupOwnerId;

    private Date createTime;

    private String groupNotice;

    // 加群方式  0：直接加入  1：管理员同意加入
    private Integer joinType;

    // 群状态  1：正常  0：解散
    private Integer status;

    // 群人数
    @TableField(exist = false)
    private Integer memberCount;

    // 群主昵称
    @TableField(exist = false)
    private String groupOwnerNickName;
}