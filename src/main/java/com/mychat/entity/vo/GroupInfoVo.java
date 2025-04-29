package com.mychat.entity.vo;

import com.mychat.entity.po.GroupInfo;
import com.mychat.entity.po.UserContact;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * projectName: com.mychat.entity.vo
 * author:  SoulGoodman-coder
 * description:
 */

@Getter
@Setter
public class GroupInfoVo {
    private GroupInfo groupInfo;
    private List<UserContact> userContactList;
}
