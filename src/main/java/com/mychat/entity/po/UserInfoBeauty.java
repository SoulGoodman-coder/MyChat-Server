package com.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @TableName user_info_beauty
 */
@TableName(value ="user_info_beauty")
@Data
public class UserInfoBeauty implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    private String email;

    private String userId;

    private Integer status;
}