package com.mychat.entity.dto;

import com.mychat.utils.enums.UserContactStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * projectName: com.mychat.entity.dto
 * author:  SoulGoodman-coder
 * description: 好友|群组 搜索结果实体类
 */
@Getter
@Setter
public class UserContactSearchDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String contactId;
    // 联系人类型 0：好友 1：群组
    private Integer contactType;
    // 联系人名称
    private String contactName;
    // 状态 0:非好友 1:好友 2:已删除好友 3:被好友删除 4:已拉黑好友 5:被好友拉黑
    private Integer status;
    private String statusName;
    private Integer sex;
    private String areaName;

    public String getStatusName() {
        UserContactStatusEnum statusEnum = UserContactStatusEnum.getByStatus(status);
        return statusEnum == null ? null : statusEnum.getDesc();
    }
}
