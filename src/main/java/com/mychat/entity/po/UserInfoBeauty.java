package com.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @TableName user_info_beauty
 */
@TableName(value ="user_info_beauty")
@Data
public class UserInfoBeauty {
    private Integer id;

    private String email;

    private String userId;

    private Integer status;
}