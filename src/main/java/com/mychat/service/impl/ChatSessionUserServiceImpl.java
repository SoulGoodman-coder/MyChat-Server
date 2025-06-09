package com.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mychat.entity.dto.MessageSendDto;
import com.mychat.entity.po.ChatSessionUser;
import com.mychat.entity.po.UserContact;
import com.mychat.mapper.UserContactMapper;
import com.mychat.service.ChatSessionUserService;
import com.mychat.mapper.ChatSessionUserMapper;
import com.mychat.utils.enums.MessageTypeEnum;
import com.mychat.utils.enums.UserContactStatusEnum;
import com.mychat.utils.enums.UserContactTypeEnum;
import com.mychat.websocket.MessageHandler;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Administrator
* @description 针对表【chat_session_user(会话用户表)】的数据库操作Service实现
* @createDate 2025-05-14 00:00:01
*/
@Service
public class ChatSessionUserServiceImpl extends ServiceImpl<ChatSessionUserMapper, ChatSessionUser>
    implements ChatSessionUserService{

    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private UserContactMapper userContactMapper;

    /**
     * 根据userId和contactId 插入或修改 applyChatSessionUser表
     *
     * @param chatSessionUser 会话用户信息对象
     */
    @Override
    public void insertOrUpdateChatSessionUser(ChatSessionUser chatSessionUser) {
        // 判断会话用户表中是否已有记录（存在则更新，不存在则插入）
        LambdaQueryWrapper<ChatSessionUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSessionUser::getUserId, chatSessionUser.getUserId())
                    .eq(ChatSessionUser::getContactId, chatSessionUser.getContactId());
        ChatSessionUser selected = chatSessionUserMapper.selectOne(queryWrapper);
        if (null == selected) {
            chatSessionUserMapper.insert(chatSessionUser);
        }else {
            LambdaUpdateWrapper<ChatSessionUser> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ChatSessionUser::getUserId, chatSessionUser.getUserId())
                         .eq(ChatSessionUser::getContactId, chatSessionUser.getContactId());
            chatSessionUserMapper.update(chatSessionUser, updateWrapper);
        }
    }

    /**
     * 根据contactId更新contactName，并发送ws消息
     * @param contactId     修改名称的用户id或群组id
     * @param contactName   修改后的名称
     */
    @Override
    public void updateContactNameByContactId(String contactId, String contactName) {
        System.out.println("22222222222222222222222222222222222");
        LambdaUpdateWrapper<ChatSessionUser> chatSessionUserUpdateWrapper = new LambdaUpdateWrapper<>();
        chatSessionUserUpdateWrapper.eq(ChatSessionUser::getContactId, contactId)
                .set(ChatSessionUser::getContactName, contactName);
        chatSessionUserMapper.update(chatSessionUserUpdateWrapper);


        UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (UserContactTypeEnum.GROUP == typeEnum){
            // 修改群昵称时，给GroupChannel发送ws消息
            MessageSendDto<Object> messageSendDto = new MessageSendDto<>();
            messageSendDto.setContactId(contactId);
            messageSendDto.setContactType(UserContactTypeEnum.GROUP.getType());
            messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
            messageSendDto.setExtendData(contactName);
            messageHandler.sendMessage(messageSendDto);
        }else {
            System.out.println("3333333333333333333333333333333333333333");
            // 修改用户昵称时，需要给该用户的每一个好友发送ws消息
            LambdaQueryWrapper<UserContact> userContactQueryWrapper = new LambdaQueryWrapper<>();
            userContactQueryWrapper.eq(UserContact::getContactId, contactId)
                                   .eq(UserContact::getContactType, UserContactTypeEnum.USER.getType())
                                   .eq(UserContact::getStatus, UserContactStatusEnum.FRIEND.getStatus());
            List<UserContact> userContactList = userContactMapper.selectList(userContactQueryWrapper);
            for (UserContact userContact : userContactList) {
                System.out.println("444444444444444444444444444444444444444444");
                MessageSendDto<Object> messageSendDto = new MessageSendDto<>();
                messageSendDto.setContactId(userContact.getUserId());
                messageSendDto.setContactType(UserContactTypeEnum.USER.getType());
                messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
                messageSendDto.setExtendData(contactName);
                messageSendDto.setSendUserId(contactId);
                messageSendDto.setSendUserNickName(contactName);
                messageHandler.sendMessage(messageSendDto);
            }
        }

    }
}




