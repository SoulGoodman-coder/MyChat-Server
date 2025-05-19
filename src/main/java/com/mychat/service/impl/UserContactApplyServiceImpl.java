package com.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mychat.entity.dto.MessageSendDto;
import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.entity.po.GroupInfo;
import com.mychat.entity.po.UserContact;
import com.mychat.entity.po.UserContactApply;
import com.mychat.entity.po.UserInfo;
import com.mychat.exception.BusinessException;
import com.mychat.mapper.GroupInfoMapper;
import com.mychat.mapper.UserContactMapper;
import com.mychat.mapper.UserInfoMapper;
import com.mychat.service.UserContactApplyService;
import com.mychat.mapper.UserContactApplyMapper;
import com.mychat.service.UserContactService;
import com.mychat.utils.StringUtils;
import com.mychat.utils.enums.*;
import com.mychat.websocket.MessageHandler;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
* @author Administrator
* @description 针对表【user_contact_apply(好友申请表)】的数据库操作Service实现
* @createDate 2025-04-19 23:22:28
*/
@Service
public class UserContactApplyServiceImpl extends ServiceImpl<UserContactApplyMapper, UserContactApply>
    implements UserContactApplyService{

    @Resource
    private UserContactApplyMapper userContactApplyMapper;

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private GroupInfoMapper groupInfoMapper;

    @Resource
    private UserContactService userContactService;

    @Resource
    private MessageHandler messageHandler;

    @Value("${contants.APPLY_INFO_TEMPLATE}")
    private String APPLY_INFO_TEMPLATE;

    /**
     * 申请添加好友、群组
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
            userContactService.addContact(applyUserId, receiveUserId, contactId, typeEnum.getType(), applyInfo);
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
            MessageSendDto<Object> messageSendDto = new MessageSendDto<>();
            messageSendDto.setMessageType(MessageTypeEnum.CONTACT_APPLY.getType());
            messageSendDto.setMessageContent(applyInfo);
            messageSendDto.setContactId(receiveUserId);
            messageHandler.sendMessage(messageSendDto);
        }

        return joinType;
    }

    /**
     * 获取好友申请列表
     *
     * @param receiveUserId 接收人id（即当前用户id）
     * @param pageNumber    页码
     * @param pageSize      页容量
     * @return List<UserContactApply>
     */
    @Override
    public List<UserContactApply> loadApply(String receiveUserId, Integer pageNumber, Integer pageSize) {
        // 判断页码参数是否合法
        if (null == pageNumber || pageNumber <= 0) {
            pageNumber = 1;
        }

        // 判断页容量参数是否合法
        if (null == pageSize || pageSize <= 0) {
            pageSize = 5;
        }

        // IPage接口的实现对象Page(当前页码, 页容量)
        Page<UserContactApply> page = new Page<>(pageNumber, pageSize);
        userContactApplyMapper.loadApply(page, receiveUserId);
        // 获取当前页数据
        List<UserContactApply> records = page.getRecords();
        return records;
    }

    /**
     * 处理加群申请或好友申请
     *
     * @param userId  当前用户id
     * @param applyId 申请的id
     * @param status  申请结果：0:待处理、1:同意、2:拒绝、3:拉黑
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dealWithApply(String userId, String applyId, Integer status) {
        // 判断申请状态是否合法
        UserContactApplyStatusEnum statusEnum = UserContactApplyStatusEnum.getByStatus(status);
        if (null == statusEnum|| UserContactApplyStatusEnum.INIT == statusEnum) {
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        // 查询id查询申请详情
        UserContactApply userContactApply = userContactApplyMapper.selectById(applyId);
        // 判断申请的接收者是否是当前用户
        if (null == userContactApply || !userId.equals(userContactApply.getReceiveUserId())) {
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        // 封装更新后的数据对象
        UserContactApply updateInfo = new UserContactApply();
        updateInfo.setStatus(status);
        updateInfo.setLastApplyTime(System.currentTimeMillis());

        // 更新数据库user_contact_apply表申请状态status字段，校验
        LambdaUpdateWrapper<UserContactApply> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserContactApply::getReceiveUserId, userId)
                     .eq(UserContactApply::getContactId, userContactApply.getContactId())
                     .eq(UserContactApply::getStatus, UserContactApplyStatusEnum.INIT.getStatus());
        int row = userContactApplyMapper.update(updateInfo, updateWrapper);
        if (row != 1){
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        // 同意申请
        if (UserContactApplyStatusEnum.PASS == statusEnum){
            // 添加联系人
            userContactService.addContact(userContactApply.getApplyUserId(), userContactApply.getReceiveUserId(), userContactApply.getContactId(), userContactApply.getContactType(), userContactApply.getApplyInfo());

            return;
        }

        // 拒接申请：只需要更新状态，不需要其他操作

        // 拉黑申请
        if (UserContactApplyStatusEnum.BLICKLIST == statusEnum){
            Date curDate = new Date();
            // 构建user_contact表数据对象
            UserContact userContact = new UserContact();
            userContact.setUserId(userContactApply.getApplyUserId());   // 发出申请的人id
            userContact.setContactId(userContactApply.getContactId());  // 被申请的用户|群组id
            userContact.setContactType(userContactApply.getContactType());
            userContact.setStatus(UserContactStatusEnum.BLACKLIST_BE_BEFORE.getStatus());


            userContactService.insertOrUpdateContact(userContact);
            return;
        }

    }

}




