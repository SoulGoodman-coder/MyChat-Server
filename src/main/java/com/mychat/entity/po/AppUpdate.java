package com.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.mychat.utils.StringUtils;
import lombok.Data;

/**
 * @TableName app_update
 */
@TableName(value ="app_update")
@Data
public class AppUpdate implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;         //自增ID

    private String version;     // 版本号

    private String updateDesc;  // 更新描述

    private Date createTime;    // 创建时间

    private Integer status;     // 状态 0:未发布 1:灰度发布 2:全网发布

    private String grayscaleUid;    // 灰度uid

    private Integer fileType;   // 文件类型 0:本地文件 1:外链

    private String outerLink;   // 外链地址

    @TableField(exist = false)
    private String[] updateDescArray;

    public String[] getUpdateDescArray() {
        if (!StringUtils.isEmpty(updateDesc)){
            return updateDesc.split("\\|");
        }
        return null;
    }
}