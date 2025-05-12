package com.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @TableName user_info_beauty
 */
@TableName(value ="user_info_beauty")
@Data
public class UserInfoBeauty {
    @TableId
    private Integer id;

    private String email;

    private String userId;

    private Integer status;
}