package com.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mychat.entity.dto.MessageSendDto;
import com.mychat.entity.dto.SysSettingDto;
import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.entity.dto.UserContactSearchDto;
import com.mychat.entity.po.*;
import com.mychat.entity.vo.UserInfoVo;
import com.mychat.exception.BusinessException;
import com.mychat.mapper.*;
import com.mychat.redis.RedisComponent;
import com.mychat.service.ChatSessionUserService;
import com.mychat.service.UserContactService;
import com.mychat.utils.CopyUtils;
import com.mychat.utils.StringUtils;
import com.mychat.utils.enums.*;
import com.mychat.websocket.ChannelContextUtils;
import com.mychat.websocket.MessageHandler;
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
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;

    @Resource
    private ChatSessionUserService chatSessionUserService;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private ChannelContextUtils channelContextUtils;

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
     * 添加联系人
     *
     * @param applyUserId   申请人id
     * @param receiveUserId 接收人id
     * @param contactId     联系人或群组id
     * @param contactType   联系人类型 0：好友 1：群组
     * @param applyInfo     申请信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            contactList.add(userContact);
        }

        // 批量插入
        this.insertOrUpdateContactList(contactList);

        // 将联系人id添加到redis缓存
        if (UserContactTypeEnum.USER.getType().equals(contactType)){
            redisComponent.saveUserContact(contactId, applyUserId);
        }
        redisComponent.saveUserContact(applyUserId, contactId);


        /*
        * 创建会话 发送消息
        * */
        // 生成sessionId
        String sessionId = null;
        if (UserContactTypeEnum.USER.getType().equals(contactType)){
            sessionId = StringUtils.getChatSessionId4User(new String[]{applyUserId, contactId});
        }else {
            sessionId = StringUtils.getChatSessionId4Group(contactId);
        }

        // 申请人信息
        UserInfo applyUserInfo = userInfoMapper.selectById(applyUserId);

        if (UserContactTypeEnum.USER.getType().equals(contactType)){    // 添加用户联系人
            /*
              会话信息表 ChatSession
             */
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(applyInfo);
            chatSession.setLastReceiveTime(curDate.getTime());
            chatSessionMapper.insertOrUpdate(chatSession);

            /*
              会话用户表 ChatSessionUser
             */
            // 申请人
            ChatSessionUser applyChatSessionUser = new ChatSessionUser();
            applyChatSessionUser.setSessionId(sessionId);
            applyChatSessionUser.setUserId(applyUserId);
            applyChatSessionUser.setContactId(contactId);
            UserInfo contactUserInfo = userInfoMapper.selectById(contactId);
            applyChatSessionUser.setContactName(contactUserInfo.getNickName());
            chatSessionUserService.insertOrUpdateChatSessionUser(applyChatSessionUser);

            // 接收人
            ChatSessionUser contactChatSessionUser = new ChatSessionUser();
            contactChatSessionUser.setSessionId(sessionId);
            contactChatSessionUser.setUserId(contactId);
            contactChatSessionUser.setContactId(applyUserId);

            contactChatSessionUser.setContactName(applyUserInfo.getNickName());
            chatSessionUserService.insertOrUpdateChatSessionUser(contactChatSessionUser);

            /*
              聊天消息表 ChatMessage
             */
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
            chatMessage.setMessageContent(applyInfo);
            chatMessage.setSendUserId(applyUserId);
            chatMessage.setSendUserNickName(applyUserInfo.getNickName());
            chatMessage.setSendTime(curDate.getTime());
            chatMessage.setContactId(contactId);
            chatMessage.setContactType(UserContactTypeEnum.USER.getType());
            chatMessageMapper.insert(chatMessage);

            MessageSendDto messageSendDto = CopyUtils.copy(chatMessage, MessageSendDto.class);
            // 发送ws消息给接收好友申请的人
            messageHandler.sendMessage(messageSendDto);

            // 发送ws消息给申请人
            messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND_SELF.getType());
            messageSendDto.setContactId(applyUserId);
            messageSendDto.setExtendData(contactUserInfo);  // 将接收人信息传给申请人
            messageHandler.sendMessage(messageSendDto);

        }else {         // 添加群组
            /*
            * 更新或修改相关chat表格
            * */
            /*
              会话信息表 ChatSession
             */
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            String sendMessage = String.format(MessageTypeEnum.ADD_GROUP.getInitMessage(), applyUserInfo.getNickName());
            chatSession.setLastMessage(sendMessage);
            chatSession.setLastReceiveTime(curDate.getTime());
            chatSessionMapper.insertOrUpdate(chatSession);

            /*
              会话用户表 ChatSessionUser
             */
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setUserId(applyUserId);
            chatSessionUser.setContactId(contactId);
            chatSessionUser.setSessionId(sessionId);
            GroupInfo groupInfo = groupInfoMapper.selectById(contactId);
            chatSessionUser.setContactName(groupInfo.getGroupName());
            chatSessionUserService.insertOrUpdateChatSessionUser(chatSessionUser);

            /*
              聊天消息表 ChatMessage
             */
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(MessageTypeEnum.ADD_GROUP.getType());
            chatMessage.setMessageContent(sendMessage);
            chatMessage.setSendTime(curDate.getTime());
            chatMessage.setContactId(contactId);
            chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
            chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
            chatMessageMapper.insert(chatMessage);

            // 将群组id作为联系人id添加到redis缓存
            redisComponent.saveUserContact(groupInfo.getGroupOwnerId(), groupInfo.getGroupId());

            // 将用户添加到groupChannel
            // channelContextUtils.addUser2Group(groupInfo.getGroupOwnerId(), groupInfo.getGroupId());
            channelContextUtils.addUser2Group(applyUserId, groupInfo.getGroupId());

            // 补全MessageSendDto属性 发送ws消息
            MessageSendDto messageSendDto = CopyUtils.copy(chatMessage, MessageSendDto.class);
            messageSendDto.setContactName(applyUserInfo.getNickName());
            LambdaQueryWrapper<UserContact> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserContact::getContactId, contactId)
                        .eq(UserContact::getStatus, UserContactStatusEnum.FRIEND.getStatus());
            Long count = userContactMapper.selectCount(queryWrapper);

            messageSendDto.setContactName(groupInfo.getGroupName());
            messageSendDto.setMemberCount(count.intValue());
            System.out.println(messageSendDto);

            messageHandler.sendMessage(messageSendDto);
        }

    }

    /**
     * 添加机器人好友
     *
     * @param userId 当前用户id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addContact4Robot(String userId) {
        Date curDate = new Date();
        // 从redis中获取机器人信息
        SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
        String robotUid = sysSettingDto.getRobotUid();
        String robotNickName = sysSettingDto.getRobotNickName();
        String robotWelcome = StringUtils.clearHtmlTag(sysSettingDto.getRobotWelcome());

        // 添加机器人好友
        UserContact userContact = new UserContact();
        userContact.setUserId(userId);
        userContact.setContactId(robotUid);
        userContact.setContactName(robotNickName);
        userContact.setContactType(UserContactTypeEnum.USER.getType());
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        userContact.setCreateTime(curDate);
        userContact.setLastUpdateTime(curDate);
        userContactMapper.insert(userContact);

        // 增加会话信息 chat_session表
        String sessionId = StringUtils.getChatSessionId4User(new String[]{userId, robotUid});
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastMessage(robotWelcome);
        chatSession.setLastReceiveTime(curDate.getTime());
        chatSessionMapper.insert(chatSession);

        // 增加会话人信息 chat_session_user表
        ChatSessionUser chatSessionUser = new ChatSessionUser();
        chatSessionUser.setUserId(userId);
        chatSessionUser.setContactId(robotUid);
        chatSessionUser.setContactName(robotNickName);
        chatSessionUser.setSessionId(sessionId);
        chatSessionUserMapper.insert(chatSessionUser);

        // 增加聊天消息 chat_message表
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(MessageTypeEnum.CHAT.getType());
        chatMessage.setMessageContent(robotWelcome);
        chatMessage.setSendUserId(robotUid);
        chatMessage.setSendUserNickName(robotNickName);
        chatMessage.setSendTime(curDate.getTime());
        chatMessage.setContactId(userId);
        chatMessage.setContactType(UserContactTypeEnum.USER.getType());
        chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        chatMessageMapper.insert(chatMessage);

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

        // 从我的好友列表缓存中删除对方
        redisComponent.removeUserContact(userId, contactId);

        // 从对方的好友列表缓存中删除我
        redisComponent.removeUserContact(contactId, userId);
    }
}




