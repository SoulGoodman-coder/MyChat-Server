package com.mychat.service;

import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.entity.dto.UserContactSearchDto;
import com.mychat.entity.po.UserContact;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mychat.entity.vo.UserInfoVo;
import com.mychat.utils.enums.UserContactStatusEnum;
import com.mychat.utils.enums.UserContactTypeEnum;

import java.util.List;

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

    /**
     * 添加联系人（数据库操作）
     * @param applyUserId       申请人id
     * @param receiveUserId     接收人id
     * @param contactId         联系人或群组id
     * @param contactType       联系人类型 0：好友 1：群组
     * @param applyInfo         申请信息
     */
    void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo);

    /**
     * 根据userId和contactId插入或更新
     * @param userContact       UserContact
     */
    void insertOrUpdateContact(UserContact userContact);

    /**
     * 根据userId和contactId批量插入或更新
     * @param userContactList   List<UserContact>
     */
    void insertOrUpdateContactList(List<UserContact> userContactList);

    /**
     * 查询当前用户的联系人列表（好友/群组）
     * @param userId            当前用户id
     * @param contactTypeEnum   联系人类型Enum
     * @return List<UserContact>
     */
    List<UserContact> loadContact(String userId, UserContactTypeEnum contactTypeEnum);

    /**
     * 获取联系人详情（可查询非好友）
     * @param userId            当前用户id
     * @param contactId         联系人id
     * @return UserInfoVo
     */
    UserInfoVo getContactInfo(String userId, String contactId);

    /**
     * 获取联系人详情（仅查询好友）
     * @param userId            当前用户id
     * @param contactId         联系人id
     * @return UserInfoVo
     */
    UserInfoVo getContactUserInfo(String userId, String contactId);

    /**
     * 拉黑或删除好友
     * @param userId            当前用户id
     * @param contactId         要拉黑或删除的用户id
     * @param statusEnum        标识要执行的操作（拉黑/删除）
     */
    void removeUserContact(String userId, String contactId, UserContactStatusEnum statusEnum);
}
