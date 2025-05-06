package com.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mychat.entity.dto.SysSettingDto;
import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.entity.dto.UserContactSearchDto;
import com.mychat.entity.po.GroupInfo;
import com.mychat.entity.po.UserContact;
import com.mychat.entity.po.UserContactApply;
import com.mychat.entity.po.UserInfo;
import com.mychat.entity.vo.UserInfoVo;
import com.mychat.exception.BusinessException;
import com.mychat.mapper.GroupInfoMapper;
import com.mychat.mapper.UserContactApplyMapper;
import com.mychat.mapper.UserInfoMapper;
import com.mychat.redis.RedisComponent;
import com.mychat.service.UserContactService;
import com.mychat.mapper.UserContactMapper;
import com.mychat.utils.CopyUtils;
import com.mychat.utils.StringUtils;
import com.mychat.utils.enums.*;
import jakarta.annotation.Resource;
import jodd.util.ArraysUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* @author Administrator
* @description 针对表【user_contact(联系人表)】的数据库操作Service实现
* @createDate 2025-04-19 23:22:20
*/
@Service
public class UserContactServiceImpl extends ServiceImpl<UserContactMapper, UserContact>
    implements UserContactService{

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private GroupInfoMapper groupInfoMapper;

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private UserContactApplyMapper userContactApplyMapper;

    @Value("${contants.APPLY_INFO_TEMPLATE}")
    private String APPLY_INFO_TEMPLATE;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 搜索好友、群组
     *
     * @param userId    当前用户id
     * @param contactId 要搜索的联系人id
     * @return UserContactSearchDto
     */
    @Override
    public UserContactSearchDto searchContact(String userId, String contactId) {
        // 判断搜索的是用户还是群组
        UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(contactId);

        if (null == typeEnum) {
            return null;
        }
        UserContactSearchDto userContactSearchDto = new UserContactSearchDto();
        switch (typeEnum) {
            case USER:
                UserInfo userInfo = userInfoMapper.selectById(contactId);
                if (null == userInfo) {
                    return null;
                }
                userContactSearchDto =  CopyUtils.copy(userInfo, UserContactSearchDto.class);
                userContactSearchDto.setContactName(userInfo.getNickName());
                break;
            case GROUP:
                GroupInfo groupInfo = groupInfoMapper.selectById(contactId);
                if (null == groupInfo) {
                    return null;
                }
                userContactSearchDto.setContactName(groupInfo.getGroupName());
                break;
        }
        userContactSearchDto.setContactId(contactId);
        userContactSearchDto.setContactType(typeEnum.getType());

        //如果搜索的是自己的id
        if (contactId.equals(userId)){
            userContactSearchDto.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            return userContactSearchDto;
        }

        // 判断是否已经是好友
        LambdaQueryWrapper<UserContact> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserContact::getContactId, contactId)
                    .eq(UserContact::getUserId, userId);
        UserContact userContact = userContactMapper.selectOne(queryWrapper);
        userContactSearchDto.setStatus(null == userContact? null : userContact.getStatus());

        return userContactSearchDto;
    }

    /**
     * 添加好友、群组
     *
     * @param tokenUserInfoDto tokenUserInfoDto
     * @param contactId        联系人id
     * @param applyInfo        申请信息
     * @return                 添加类型
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer applyAdd(TokenUserInfoDto tokenUserInfoDto, String contactId, String applyInfo) {
        // 获取联系人类型
        UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (null == typeEnum) {
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        // 申请人id
        String applyUserId = tokenUserInfoDto.getUserId();
        // 申请信息
        applyInfo = StringUtils.isEmpty(applyInfo)? String.format(APPLY_INFO_TEMPLATE, tokenUserInfoDto.getNickName()) : applyInfo;
        // 当前时间
        long curTime = System.currentTimeMillis();
        // 申请类型（好友|群组）
        Integer joinType = null;
        // 接收申请的对象id
        String receiveUserId = contactId;

        // 查询对方好友是否已经添加，如果已拉黑无法添加
        LambdaQueryWrapper<UserContact> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserContact::getContactId, contactId)
                .eq(UserContact::getUserId, applyUserId);
        UserContact userContact = userContactMapper.selectOne(queryWrapper);
        if (null != userContact &&
                (UserContactStatusEnum.BLACKLIST_BE_BEFORE.getStatus().equals(userContact.getStatus()) || UserContactStatusEnum.BLACKLIST_BE_AFTER.getStatus().equals(userContact.getStatus()))
        ) {
            throw new BusinessException("对方已将你拉黑，无法添加");
        }

        // 判断申请加好友 还是申请加群组
        switch (typeEnum){
            case GROUP:
                GroupInfo groupInfo = groupInfoMapper.selectById(contactId);
                if (null == groupInfo || GroupStatusEnum.DISSOLUTION.getStatus().equals(groupInfo.getStatus())) {
                    throw new BusinessException("群聊不存在或已解散");
                }
                receiveUserId = groupInfo.getGroupOwnerId();
                joinType = groupInfo.getJoinType();
                break;
            case USER:
                UserInfo userInfo = userInfoMapper.selectById(contactId);
                if (null == userInfo) {
                    throw new BusinessException(ResultCodeEnum.CODE_600);
                }
                receiveUserId = userInfo.getUserId();
                joinType = userInfo.getJoinType();
                break;
        }

        // joinType为直接加入，不用发申请，直接加好友
        if (JoinTypeEnum.JOIN.getType().equals(joinType)){
            this.addContact(applyUserId, receiveUserId, contactId, typeEnum.getType(), applyInfo);
            return joinType;
        }

        // 从申请表user_contact-apply中查询申请信息
        LambdaQueryWrapper<UserContactApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserContactApply::getApplyUserId, applyUserId)
                .eq(UserContactApply::getContactId, contactId);
        UserContactApply dbApply = userContactApplyMapper.selectOne(wrapper);

        // 判断申请信息是否存在（即是否第一次申请）
        if (null == dbApply) {
            UserContactApply userContactApply = new UserContactApply();
            userContactApply.setApplyUserId(applyUserId);
            userContactApply.setContactType(typeEnum.getType());
            userContactApply.setReceiveUserId(receiveUserId);
            userContactApply.setContactId(contactId);
            userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            userContactApply.setApplyInfo(applyInfo);
            userContactApply.setLastApplyTime(curTime);
            userContactApplyMapper.insert(userContactApply);
        }else{
            // 更新状态
            UserContactApply userContactApply = new UserContactApply();
            userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            userContactApply.setApplyInfo(applyInfo);
            userContactApply.setLastApplyTime(curTime);
            userContactApply.setApplyId(dbApply.getApplyId());
            userContactApplyMapper.updateById(userContactApply);
        }

        // 发送ws消息
        if (null == dbApply || !UserContactApplyStatusEnum.INIT.getStatus().equals(dbApply.getStatus())){
            // TODO 发送ws消息

        }

        return joinType;
    }

    /**
     * 添加联系人
     *
     * @param applyUserId   申请人id
     * @param receiveUserId 接收人id
     * @param contactId     联系人或群组id
     * @param contactType   联系人类型 0：好友 1：群组
     * @param applyInfo     申请信息
     */
    @Override
    public void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo) {
        // 判断群聊人数是否超出
        if (UserContactTypeEnum.GROUP.getType().equals(contactType)){
            LambdaQueryWrapper<UserContact> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserContact::getContactId, contactId)
                    .eq(UserContact::getStatus, UserContactStatusEnum.FRIEND.getStatus());
            Long count = userContactMapper.selectCount(queryWrapper);
            SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
            if (count >= sysSettingDto.getMaxGroupMemberCount()) {
                throw new BusinessException("群聊" + contactId + "成员已满，无法加入");
            }
        }

        Date curDate = new Date();
        ArrayList<UserContact> contactList = new ArrayList<>();

        // 申请人添加接收人为好友
        UserContact userContact = new UserContact();
        userContact.setUserId(applyUserId);
        userContact.setContactId(contactId);
        userContact.setContactType(contactType);
        userContact.setCreateTime(curDate);
        userContact.setLastUpdateTime(curDate);
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        contactList.add(userContact);

        // 接收人添加申请人为好友（群组则接收人无需添加申请人为好友）
        if (UserContactTypeEnum.USER.getType().equals(contactType)){
            userContact = new UserContact();
            userContact.setUserId(contactId);
            userContact.setContactId(applyUserId);
            userContact.setContactType(contactType);
            // userContact.setCreateTime(curDate);
            // userContact.setLastUpdateTime(curDate);
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            contactList.add(userContact);
        }

        // 批量插入
        this.insertOrUpdateContactList(contactList);

        // TODO 添加缓存

        // TODO 创建会话 发送消息
    }

    /**
     * 根据userId和contactId插入或更新
     *
     * @param userContact UserContact
     */
    @Override
    public void insertOrUpdateContact(UserContact userContact) {
        Date curDate = new Date();
        userContact.setLastUpdateTime(curDate);

        // 判断user_contact表中是否已经存在该条好友记录。存在则更新，不存在则插入
        LambdaQueryWrapper<UserContact> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserContact::getUserId, userContact.getUserId())
                .eq(UserContact::getContactId, userContact.getContactId());
        UserContact selectedUserContact = userContactMapper.selectOne(queryWrapper);

        if (null == selectedUserContact) {
            userContact.setCreateTime(curDate);
            userContactMapper.insert(userContact);
        }else {
            LambdaUpdateWrapper<UserContact> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(UserContact::getUserId, userContact.getUserId())
                    .eq(UserContact::getContactId, userContact.getContactId());
            userContactMapper.update(userContact, wrapper);
        }
    }

    /**
     * 根据userId和contactId批量插入或更新
     *
     * @param userContactList List<UserContact>
     */
    @Override
    public void insertOrUpdateContactList(List<UserContact> userContactList) {
        userContactList.forEach(this::insertOrUpdateContact);
    }

    /**
     * 查询当前用户的联系人列表（好友/群组）
     *
     * @param userId 当前用户id
     * @param contactTypeEnum   联系人类型Enum
     * @return List<UserContact>
     */
    @Override
    public List<UserContact> loadContact(String userId, UserContactTypeEnum contactTypeEnum) {
        // 判断联系人类型
        if (UserContactTypeEnum.USER == contactTypeEnum){
            return userContactMapper.getUserContectList(userId);
        } else if (UserContactTypeEnum.GROUP == contactTypeEnum) {
            return userContactMapper.getGroupContactList(userId);
        }
        return null;
    }

    /**
     * 获取联系人详情（可查询非好友）
     *
     * @param userId    当前用户id
     * @param contactId 联系人id
     * @return UserInfoVo
     */
    @Override
    public UserInfoVo getContactInfo(String userId, String contactId) {
        // 根据id查询用户信息，拷贝到UserInfoVo对象中
        UserInfo userInfo = userInfoMapper.selectById(contactId);
        UserInfoVo userInfoVo = CopyUtils.copy(userInfo, UserInfoVo.class);

        userInfoVo.setContactStatus(UserContactStatusEnum.NOT_FRIEND.getStatus());

        // 判断要查询的联系人与当前用户是否是好友
        LambdaQueryWrapper<UserContact> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserContact::getUserId, userId)
                    .eq(UserContact::getContactId, contactId);
        UserContact userContact = userContactMapper.selectOne(queryWrapper);
        if (null != userContact){
            userInfoVo.setContactStatus(UserContactStatusEnum.FRIEND.getStatus());
        }

        return userInfoVo;
    }

    /**
     * 获取联系人详情（仅查询好友）
     *
     * @param userId    当前用户id
     * @param contactId 联系人id
     * @return UserInfoVo
     */
    @Override
    public UserInfoVo getContactUserInfo(String userId, String contactId) {
        // 判断要查询的联系人与当前用户是否是好友
        LambdaQueryWrapper<UserContact> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserContact::getUserId, userId)
                .eq(UserContact::getContactId, contactId);
        UserContact userContact = userContactMapper.selectOne(queryWrapper);
        // 非好友，或联系人状态异常
        if (null == userContact ||
                !ArraysUtil.contains(new Integer[]{
                        UserContactStatusEnum.FRIEND.getStatus(),
                        UserContactStatusEnum.DEL_BE.getStatus(),
                        UserContactStatusEnum.BLACKLIST_BE_AFTER.getStatus()
                }, userContact.getStatus())){
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }
        UserInfo userInfo = userInfoMapper.selectById(contactId);
        return CopyUtils.copy(userInfo, UserInfoVo.class);
    }

    /**
     * 拉黑或删除好友
     *
     * @param userId     当前用户id
     * @param contactId  要拉黑或删除的用户id
     * @param statusEnum 标识要执行的操作（拉黑/删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUserContact(String userId, String contactId, UserContactStatusEnum statusEnum) {
        // 在自己的好友列表中移除对方
        LambdaUpdateWrapper<UserContact> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserContact::getUserId, userId)
                     .eq(UserContact::getContactId, contactId)
                     .set(UserContact::getStatus, statusEnum.getStatus());
        userContactMapper.update(updateWrapper);

        // 在对方的好友列表中移除自己
        updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserContact::getUserId, contactId)
                .eq(UserContact::getContactId, userId);
        if (UserContactStatusEnum.DEL == statusEnum){
            updateWrapper.set(UserContact::getStatus, UserContactStatusEnum.DEL_BE.getStatus());
        } else if (UserContactStatusEnum.BLACKLIST == statusEnum) {
            updateWrapper.set(UserContact::getStatus, UserContactStatusEnum.BLACKLIST_BE_AFTER.getStatus());
        }
        userContactMapper.update(updateWrapper);

        // TODO 从我的好友列表缓存中删除对方

        // TODO 从对方的好友列表缓存中删除我
    }
}




