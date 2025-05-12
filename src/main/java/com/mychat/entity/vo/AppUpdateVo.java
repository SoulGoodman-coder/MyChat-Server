package com.mychat.entity.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * projectName: com.mychat.entity.vo
 * author:  SoulGoodman-coder
 * description: app更新
 */

@Getter
@Setter
public class AppUpdateVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;     //自增ID

    private String version;     // 版本号

    private String[] updateDescArray;   // 更新描述

    private Long size;      // 文件大小

    private String fileName;    // 文件名

    private Integer fileType;   // 文件类型 0:本地文件 1:外链

    private String outerLink;   // 外链地址

}
