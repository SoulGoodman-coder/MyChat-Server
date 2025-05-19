package com.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mychat.entity.config.AppConfig;
import com.mychat.entity.dto.MessageSendDto;
import com.mychat.entity.dto.SysSettingDto;
import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.entity.po.*;
import com.mychat.exception.BusinessException;
import com.mychat.mapper.*;
import com.mychat.redis.RedisComponent;
import com.mychat.service.ChatSessionUserService;
import com.mychat.service.GroupInfoService;
import com.mychat.service.UserContactService;
import com.mychat.utils.CopyUtils;
import com.mychat.utils.StringUtils;
import com.mychat.utils.enums.*;
import com.mychat.websocket.ChannelContextUtils;
import com.mychat.websocket.MessageHandler;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
* @author Administrator
* @description 针对表【group_info(群组表)】的数据库操作Service实现
* @createDate 2025-04-19 23:21:32
*/
@Service
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo> implements GroupInfoService{

    @Resource
    private GroupInfoMapper groupInfoMapper;

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appConfig;

    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;

    @Resource
    private ChatSessionUserService chatSessionUserService;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private ChannelContextUtils channelContextUtils;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private UserContactService userContactService;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    @Lazy
    private GroupInfoService groupInfoService;

    @Value("${contants.FILE_FOLDER_FILE}")
    private String FILE_FOLDER_FILE;

    @Value("${contants.FILE_FOLDER_AVATAR_NAME}")
    private String FILE_FOLDER_AVATAR_NAME;

    @Value("${contants.PNG_SUFFIX}")
    private String PNG_SUFFIX;

    @Value("${contants.COVER_PNG_SUFFIX}")
    private String COVER_PNG_SUFFIX;

    /**
     * 创建或修改群聊
     *
     * @param groupInfo     群组信息
     * @param avatarFile 原群头像
     * @param avatarCover   群头像缩略图
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
        // 当前时间
        Date curDate = new Date();

        //判断创建群聊 还是修改群聊
        if (StringUtils.isEmpty(groupInfo.getGroupId())){   // 创建群聊
            // 查询已创建群聊数
            LambdaQueryWrapper<GroupInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(GroupInfo::getGroupOwnerId, groupInfo.getGroupOwnerId());
            Long count = groupInfoMapper.selectCount(queryWrapper);

            // 判断群聊数是否超出
            SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
            if (count >= sysSettingDto.getMaxGroupCount()){
                throw new BusinessException("最多只能创建" + sysSettingDto.getMaxGroupCount() + "个群聊");
            }

            // 判断群头像参数是否为空
            if (null == avatarCover){
                throw new BusinessException(ResultCodeEnum.CODE_600);
            }

            // 补全参数
            groupInfo.setCreateTime(curDate);
            groupInfo.setGroupId(StringUtils.getGroupId());

            // 写入数据库
            groupInfoMapper.insert(groupInfo);

            // 将群组添加为联系人
            UserContact userContact = new UserContact();
            userContact.setUserId(groupInfo.getGroupOwnerId());
            userContact.setContactId(groupInfo.getGroupId());
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContact.setContactType(UserContactTypeEnum.GROUP.getType());
            userContact.setCreateTime(curDate);
            userContact.setLastUpdateTime(curDate);

            // 写入数据库
            userContactMapper.insert(userContact);

            /*
            * 创建会话 发送消息
            * */

            // 生成sessionId
            String sessionId = StringUtils.getChatSessionId4Group(groupInfo.getGroupId());
            /*
              会话信息表 ChatSession
             */
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSession.setLastReceiveTime(curDate.getTime());
            chatSessionMapper.insertOrUpdate(chatSession);

            /*
              会话用户表 ChatSessionUser
             */
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setUserId(groupInfo.getGroupOwnerId());
            chatSessionUser.setContactId(groupInfo.getGroupId());
            chatSessionUser.setContactName(groupInfo.getGroupName());
            chatSessionUser.setSessionId(sessionId);
            chatSessionUserMapper.insert(chatSessionUser);

            /*
              聊天消息表 ChatMessage
             */
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(MessageTypeEnum.GROUP_CREATE.getType());
            chatMessage.setMessageContent(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatMessage.setSendTime(curDate.getTime());
            chatMessage.setContactId(groupInfo.getGroupId());
            chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
            chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
            chatMessageMapper.insert(chatMessage);

            // 将群组id作为联系人id添加到redis缓存
            redisComponent.saveUserContact(groupInfo.getGroupOwnerId(), groupInfo.getGroupId());

            // 将用户添加到groupChannel
            channelContextUtils.addUser2Group(groupInfo.getGroupOwnerId(), groupInfo.getGroupId());
            /*
            * 发送ws消息
            * */
            // 补全chatSessionUser属性
            chatSessionUser.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSessionUser.setLastReceiveTime(curDate.getTime());
            chatSessionUser.setMemberCount(1);

            MessageSendDto messageSendDto = CopyUtils.copy(chatMessage, MessageSendDto.class);
            messageSendDto.setExtendDate(chatSessionUser);
            messageSendDto.setLastMessage(chatSessionUser.getLastMessage());
            channelContextUtils.sendMessage(messageSendDto);
        }else {                                             // 修改群聊
            // 根据groupId查询GroupInfo
            LambdaQueryWrapper<GroupInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(GroupInfo::getGroupId, groupInfo.getGroupId());
            GroupInfo dbGroupInfo = groupInfoMapper.selectOne(queryWrapper);

            // 判断当前用户是否是群主（不是群主，不允许修改操作）
            if (! dbGroupInfo.getGroupOwnerId().equals(groupInfo.getGroupOwnerId())){
                throw new BusinessException(ResultCodeEnum.CODE_600);
            }

            // 将修改数据写入数据库
            groupInfoMapper.updateById(groupInfo);

            // 若群名称修改，更新会话用户表ChatSessionUser信息
            String groupNameUpdate = groupInfo.getGroupName();
            if (dbGroupInfo.getGroupName().equals(groupNameUpdate)){
                return;
            }
            chatSessionUserService.updateContactNameByContactId(groupInfo.getGroupId(), groupNameUpdate);
        }

        /*
         * 上传群头像
         */
        if (null == avatarCover){
            return;
        }
        // 构建存储路径
        String baseFolder = appConfig.getProjectFolder() + FILE_FOLDER_FILE;
        File targetFileFolder = new File(baseFolder + FILE_FOLDER_AVATAR_NAME);
        if (!targetFileFolder.exists()){
            targetFileFolder.mkdirs();
        }
        String filePath = targetFileFolder.getPath() + "/" + groupInfo.getGroupId() + PNG_SUFFIX;

        avatarFile.transferTo(new File(filePath));
        avatarCover.transferTo(new File(filePath + COVER_PNG_SUFFIX));
    }

    /**
     * 获取我创建的群组
     *
     * @param userId 用户id
     * @return List<GroupInfo>
     */
    @Override
    public List<GroupInfo> loadMyGroup(String userId) {
        LambdaQueryWrapper<GroupInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupInfo::getGroupOwnerId, userId);
        queryWrapper.orderByDesc(GroupInfo::getCreateTime);

        return groupInfoMapper.selectList(queryWrapper);
    }

    /**
     * 获取群聊详情
     *
     * @param userId  当前用户id
     * @param groupId 群组id
     */
    @Override
    public GroupInfo getGroupInfo(String userId, String groupId) {
        // 判断当前用户是否在群组中（即群组是否是用户联系人）
        LambdaQueryWrapper<UserContact> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserContact::getUserId, userId)
                    .eq(UserContact::getContactId, groupId);
        UserContact userContact = userContactMapper.selectOne(queryWrapper);
        if (null == userContact|| !UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())){
            throw new BusinessException("你不在当前群聊中，或群聊不存在已解散");
        }

        // 查询GroupInfo
        LambdaQueryWrapper<GroupInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupInfo::getGroupId, groupId);
        GroupInfo groupInfo = groupInfoMapper.selectOne(wrapper);

        // 校验GroupInfo
        if (null == groupInfo || !GroupStatusEnum.NORMAL.getStatus().equals(groupInfo.getStatus())){
            throw new BusinessException("当前群聊不存在或已解散");
        }

        return groupInfo;
    }

    /**
     * 获取群人数
     *
     * @param groupId 群组id
     * @return 群人数
     */
    @Override
    public Integer getGroupMemberCount(String groupId) {
        LambdaQueryWrapper<UserContact> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserContact::getContactId, groupId);
        return Math.toIntExact(userContactMapper.selectCount(queryWrapper));
    }

    /**
     * 获取群成员列表
     *
     * @param groupId 群组id
     * @return 群成员列表
     */
    @Override
    public List<UserContact> getGroupUserContactList(String groupId) {
        return userContactMapper.getGroupUserContactList(groupId);
    }

    /**
     * 获取群组列表
     *
     * @param groupId        群组id
     * @param groupNameFuzzy 群组名称（支持模糊搜索）
     * @param groupOwnerId   群主id
     * @param pageNumber     页码
     * @param pageSize       页容量
     * @return List<GroupInfo>
     */
    @Override
    public List<GroupInfo> loadGroupList(String groupId, String groupNameFuzzy, String groupOwnerId, Integer pageNumber, Integer pageSize) {
        // 判断页码参数是否合法
        if (null == pageNumber || pageNumber <= 0) {
            pageNumber = 1;
        }

        // 判断页容量参数是否合法
        if (null == pageSize || pageSize <= 0) {
            pageSize = 15;
        }

        // IPage接口的实现对象Page(当前页码, 页容量)
        Page<GroupInfo> page = new Page<>(pageNumber, pageSize);
        groupInfoMapper.loadGroupList(page, groupId, groupNameFuzzy, groupOwnerId);

        // 获取当前页数据
        List<GroupInfo> records = page.getRecords();
        return records;
    }

    /**
     * 解散群组
     *
     * @param userId  当前用户id
     * @param groupId 要紧解散的群组id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dissolutionGroup(String userId, String groupId) {
        // 判断当前用户是否是群主
        GroupInfo dbGroupInfo = groupInfoMapper.selectById(groupId);
        if (null == dbGroupInfo || !dbGroupInfo.getGroupOwnerId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        // 删除群组
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(groupId);
        groupInfo.setStatus(GroupStatusEnum.DISSOLUTION.getStatus());
        groupInfoMapper.updateById(groupInfo);

        // 删除联系人
        LambdaUpdateWrapper<UserContact> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserContact::getContactId, groupId)
                     .eq(UserContact::getContactType, UserContactTypeEnum.GROUP.getType())
                     .set(UserContact::getStatus, UserContactStatusEnum.DEL.getStatus());
        userContactMapper.update(updateWrapper);

        // 移除群员的联系人缓存
        LambdaQueryWrapper<UserContact> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserContact::getContactId, groupId)
                    .eq(UserContact::getContactType, UserContactTypeEnum.GROUP.getType());
        List<UserContact> userContactList = userContactMapper.selectList(queryWrapper);
        for (UserContact userContact : userContactList) {
            redisComponent.removeUserContact(userContact.getUserId(), userContact.getContactId());
        }

        /*
        * 更新相关表 发送ws消息
        * */
        // 更新会话信息表 chatSession
        Date curDate = new Date();
        String sessionId = StringUtils.getChatSessionId4Group(groupId);
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastMessage(MessageTypeEnum.DISSOLUTION_GROUP.getInitMessage());
        chatSession.setLastReceiveTime(curDate.getTime());
        chatSessionMapper.updateById(chatSession);

        // 更新聊天消息表 chatMessage
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(MessageTypeEnum.DISSOLUTION_GROUP.getType());
        chatMessage.setMessageContent(MessageTypeEnum.DISSOLUTION_GROUP.getInitMessage());
        chatMessage.setContactId(groupId);
        chatMessage.setSendTime(curDate.getTime());
        chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
        chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        chatMessageMapper.insert(chatMessage);

        // 发送ws消息
        MessageSendDto messageSendDto = CopyUtils.copy(chatMessage, MessageSendDto.class);
        messageHandler.sendMessage(messageSendDto);

    }

    /**
     * 群组添加或移除人员
     *
     * @param tokenUserInfoDto 用户token对象
     * @param groupId          群组id
     * @param selectContacts   选择的要操作的人员
     * @param opType           操作类型 添加 移除
     */
    @Override
    public void addOrRemoveGroupUser(TokenUserInfoDto tokenUserInfoDto, String groupId, String selectContacts, Integer opType) {
        // 参数校验
        GroupInfo groupInfo = groupInfoMapper.selectById(tokenUserInfoDto.getUserId());
        if (null == groupInfo || !groupInfo.getGroupOwnerId().equals(tokenUserInfoDto.getUserId())) {
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        String[] contactIds = selectContacts.split(",");
        for (String contactId : contactIds) {
            if (GroupUserOpTypeEnum.ADD.getType().equals(opType)){  // 添加
                userContactService.addContact(contactId, null, groupId, UserContactTypeEnum.GROUP.getType(), null);
            } else if (GroupUserOpTypeEnum.REMOVE.getType().equals(opType)) {   // 移除
                /*
                * 此处不能直接调用当前类中的leaveGroup方法
                * 直接调用的话，leaveGroup没有交给spring管理，上面的@Transactional事务不生效
                * 通过@Resource注入GroupInfoService，通过注入的对象调用leaveGroup则事务生效
                * 同时添加@Lazy注解懒加载，防止启动时循环调用报错
                * */
                groupInfoService.leaveGroup(contactId, groupId, MessageTypeEnum.REMOVE_GROUP);
            }else {
                throw new BusinessException(ResultCodeEnum.CODE_600);
            }
        }
    }

    /**
     * 退群（被踢出群组）
     *
     * @param userId          用户id
     * @param groupId         群组id
     * @param messageTypeEnum 消息类型
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum) {
        // 参数校验（群主无法退出群聊，只能解散群聊）
        GroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        if (null == groupInfo ||groupInfo.getGroupOwnerId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        // 更新UserContact表
        LambdaQueryWrapper<UserContact> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserContact::getUserId, userId)
                    .eq(UserContact::getContactId, groupId);
        int delete = userContactMapper.delete(queryWrapper);
        if (delete == 0){
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        UserInfo userInfo = userInfoMapper.selectById(userId);
        Date curDate = new Date();
        String sessionId = StringUtils.getChatSessionId4Group(groupId);
        String messageContact = String.format(messageTypeEnum.getInitMessage(), userInfo.getNickName());

        // 更新chatSession表
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastMessage(messageContact);
        chatSession.setLastReceiveTime(curDate.getTime());
        chatSessionMapper.updateById(chatSession);

        // 更新chatMessage表
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(messageTypeEnum.getType());
        chatMessage.setMessageContent(messageContact);
        chatMessage.setContactId(groupId);
        chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
        chatMessage.setSendTime(curDate.getTime());
        chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        chatMessageMapper.insert(chatMessage);

        // 更新群人数
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserContact::getContactId, groupId)
                    .eq(UserContact::getStatus, UserContactStatusEnum.FRIEND.getStatus());
        Long memberCount = userContactMapper.selectCount(queryWrapper);
        MessageSendDto messageSendDto = CopyUtils.copy(chatMessage, MessageSendDto.class);
        messageSendDto.setMemberCount(memberCount.intValue());
        messageSendDto.setExtendDate(userId);
        messageHandler.sendMessage(messageSendDto);
    }
}




