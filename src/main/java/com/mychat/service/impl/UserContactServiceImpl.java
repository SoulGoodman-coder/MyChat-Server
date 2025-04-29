package com.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.entity.dto.UserContactSearchDto;
import com.mychat.entity.po.GroupInfo;
import com.mychat.entity.po.UserContact;
import com.mychat.entity.po.UserContactApply;
import com.mychat.entity.po.UserInfo;
import com.mychat.exception.BusinessException;
import com.mychat.mapper.GroupInfoMapper;
import com.mychat.mapper.UserContactApplyMapper;
import com.mychat.mapper.UserInfoMapper;
import com.mychat.service.UserContactService;
import com.mychat.mapper.UserContactMapper;
import com.mychat.utils.CopyUtils;
import com.mychat.utils.StringUtils;
import com.mychat.utils.enums.*;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (null != userContact && UserContactStatusEnum.BLACKLIST_BE.getStatus().equals(userContact.getStatus())) {
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
            // TODO 加为联系人

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
}




