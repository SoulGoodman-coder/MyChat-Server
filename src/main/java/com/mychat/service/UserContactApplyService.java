package com.mychat.service;

import com.mychat.entity.po.UserContact;
import com.mychat.entity.po.UserContactApply;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Administrator
* @description 针对表【user_contact_apply(好友申请表)】的数据库操作Service
* @createDate 2025-04-19 23:22:28
*/
public interface UserContactApplyService extends IService<UserContactApply> {

    /**
     * 获取好友申请列表
     * @param receiveUserId     接收人id（即当前用户id）
     * @param pageNumber        页码
     * @return List<UserContactApply>
     */
    List<UserContactApply> loadApply(String receiveUserId, Integer pageNumber);

    /**
     * 处理加群申请或好友申请
     * @param userId        当前用户id
     * @param applyId       申请的id
     * @param status        申请结果：0:待处理、1:同意、2:拒绝、3:拉黑
     */
    void dealWithApply(String userId, String applyId, Integer status);

}
