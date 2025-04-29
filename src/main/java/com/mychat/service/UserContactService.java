package com.mychat.service;

import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.entity.dto.UserContactSearchDto;
import com.mychat.entity.po.UserContact;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【user_contact(联系人表)】的数据库操作Service
* @createDate 2025-04-19 23:22:20
*/
public interface UserContactService extends IService<UserContact> {

    /**
     * 搜索好友、群组
     * @param userId        当前用户id
     * @param contactId     要搜索的联系人id
     * @return UserContactSearchDto
     */
    UserContactSearchDto searchContact(String userId, String contactId);

    /**
     * 添加好友、群组
     * @param tokenUserInfoDto  tokenUserInfoDto
     * @param contactId         联系人id
     * @param applyInfo         申请信息
     * @return                  添加类型
     */
    Integer applyAdd(TokenUserInfoDto tokenUserInfoDto, String contactId, String applyInfo);
}
