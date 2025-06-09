package com.mychat.websocket;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mychat.entity.dto.MessageSendDto;
import com.mychat.entity.dto.WsInitDateDto;
import com.mychat.entity.po.ChatMessage;
import com.mychat.entity.po.ChatSessionUser;
import com.mychat.entity.po.UserContactApply;
import com.mychat.entity.po.UserInfo;
import com.mychat.mapper.ChatMessageMapper;
import com.mychat.mapper.ChatSessionUserMapper;
import com.mychat.mapper.UserContactApplyMapper;
import com.mychat.mapper.UserInfoMapper;
import com.mychat.redis.RedisComponent;
import com.mychat.utils.JsonUtils;
import com.mychat.utils.StringUtils;
import com.mychat.utils.enums.MessageTypeEnum;
import com.mychat.utils.enums.UserContactApplyStatusEnum;
import com.mychat.utils.enums.UserContactTypeEnum;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * projectName: com.mychat.websocket
 * author:  SoulGoodman-coder
 * description: 管理channel
 */

@Component
@Slf4j
public class ChannelContextUtils {

    // 将Channel存入Map中
    private static final ConcurrentHashMap<String, Channel> USER_CHANNEL_CONTEXT_MAP = new ConcurrentHashMap<>();           // 单聊
    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CHANNEL_CONTEXT_MAP = new ConcurrentHashMap<>();     // 群聊

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private UserContactApplyMapper userContactApplyMapper;

    @Value("${contants.TIME_MILLISECONDS_1DAY}")
    private long TIME_MILLISECONDS_1DAY;

    /**
     * 用户上线
     * 1、将channel和userId绑定到attribute上，可以通过channel获取userId
     * 2、同时将channel加入Group
     * 3、更新用户信息
     * 4、给用户发送离线期间接收的消息
     * @param userId        用户id
     * @param channel       通道
     */
    public void addContext(String userId, Channel channel){

        String channelId = channel.id().toString();

        AttributeKey attributeKey = null;
        if (!attributeKey.exists(channelId)){
            attributeKey = AttributeKey.newInstance(channelId);
        }else {
            attributeKey = AttributeKey.valueOf(channelId);
        }

        channel.attr(attributeKey).set(userId);

        // 从redis中获取当前用户联系人id列表
        List<String> userContactIdList = redisComponent.getUserContactIdList(userId);
        // 若联系人是群组，则将channel加入Group
        for (String userContactId: userContactIdList){
            if (userContactId.startsWith(UserContactTypeEnum.GROUP.getPrefix())){
                add2Group(userContactId, channel);
            }
        }

        // 将channel写入内存
        USER_CHANNEL_CONTEXT_MAP.put(userId, channel);
        // 更新心跳
        redisComponent.saveUserHeartBeat(userId);
        // 更新用户最后登录时间
        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUserId(userId);
        updateUserInfo.setLastLoginTime(new Date());
        userInfoMapper.updateById(updateUserInfo);

        // 查询用户离线时间（给用户发送离线期间接收的消息）
        UserInfo userInfo = userInfoMapper.selectById(userId);
        Long lastOffTime = userInfo.getLastOffTime();
        if (null != lastOffTime && System.currentTimeMillis() - lastOffTime > TIME_MILLISECONDS_1DAY*3){
            // 离线超过3天 则发送三天内的消息
            lastOffTime = System.currentTimeMillis() - TIME_MILLISECONDS_1DAY*3;
        }

        WsInitDateDto wsInitDateDto = new WsInitDateDto();
        // 查询会话信息
        List<ChatSessionUser> chatSessionUserList = chatSessionUserMapper.selectChatSessionInfoList(userId);
        wsInitDateDto.setChatSessionUserList(chatSessionUserList);

        // 查询聊天消息 即：chat_message表中contactId为当前用户id和当前加入群组id的数据
        // 从当前用户联系人id列表中过滤出 群组id列表
        List<String> groupIdList = userContactIdList.stream().filter(item -> item.startsWith(UserContactTypeEnum.GROUP.getPrefix())).collect(Collectors.toList());
        groupIdList.add(userId);
        List<ChatMessage> chatMessageList = chatMessageMapper.selectChatMessageByContactIdList(groupIdList, lastOffTime);
        wsInitDateDto.setChatMessageList(chatMessageList);

        // 查询好友申请
        LambdaQueryWrapper<UserContactApply> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserContactApply::getReceiveUserId, userId)
                .eq(UserContactApply::getStatus, UserContactApplyStatusEnum.INIT.getStatus())
                .gt(UserContactApply::getLastApplyTime, lastOffTime);
        Integer applyCount = userContactApplyMapper.selectCount(queryWrapper).intValue();
        wsInitDateDto.setApplyCount(applyCount);

        // 给当前登录用户客户端发送ws消息
        MessageSendDto<WsInitDateDto> messageSendDto = new MessageSendDto<>();
        messageSendDto.setMessageType(MessageTypeEnum.INIT.getType());
        messageSendDto.setContactId(userId);
        messageSendDto.setExtendData(wsInitDateDto);
        sendMessage(messageSendDto);
    }

    /**
     * 发送ws消息
     * @param messageSendDto    ws消息对象
     */
    public void sendMessage(MessageSendDto messageSendDto){
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(messageSendDto.getContactId());
        switch (contactTypeEnum){
            // 给用户发送ws消息
            case USER:
                send2User(messageSendDto);
                break;
            // 给群组发送ws消息
            case GROUP:
                send2Group(messageSendDto);
                break;
        }

    }

    /**
     * 给用户发送ws消息
     * @param messageSendDto    ws消息对象
     */
    private void send2User(MessageSendDto messageSendDto){
        String contactId = messageSendDto.getContactId();
        if (StringUtils.isEmpty(contactId)){
            return;
        }
        sendMsg(contactId, messageSendDto);

        // 强制下线
        if (MessageTypeEnum.FORCE_OFF_LINE.getType().equals(messageSendDto.getMessageType())){
            closeContext(contactId);
        }
    }

    /**
     * 给群聊发送ws消息
     * @param messageSendDto    ws消息对象
     */
    private void send2Group(MessageSendDto messageSendDto){
        // 获取群聊id
        String groupId = messageSendDto.getContactId();
        if (StringUtils.isEmpty(groupId)){
            return;
        }

        // 获取channelGroup
        ChannelGroup channelGroup = GROUP_CHANNEL_CONTEXT_MAP.get(groupId);
        if (null == channelGroup){
            return;
        }
        channelGroup.writeAndFlush(new TextWebSocketFrame(JsonUtils.covertObj2Json(messageSendDto)));

        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(messageSendDto.getMessageType());
        // 退出群聊或被踢出群聊
        if (MessageTypeEnum.LEAVE_GROUP == messageTypeEnum || MessageTypeEnum.REMOVE_GROUP == messageTypeEnum){
            String userId = (String) messageSendDto.getExtendData();
            redisComponent.removeUserContact(userId, messageSendDto.getContactId());
            Channel channel = USER_CHANNEL_CONTEXT_MAP.get(userId);
            if (null != channel){
                channelGroup.remove(channel);
            }
        }
        // 解散群聊
        if (MessageTypeEnum.DISSOLUTION_GROUP == messageTypeEnum){
            GROUP_CHANNEL_CONTEXT_MAP.remove(groupId);
            channelGroup.close();
        }

    }

    /**
     * 给单个用户发送ws消息
     * @param receiveId         接收人id
     * @param messageSendDto    发送的ws消息对象
     */
    public static void sendMsg(String receiveId, MessageSendDto messageSendDto){
        if (StringUtils.isEmpty(receiveId)){
            return;
        }

        // 接收人的channel
        Channel receiveChannel = USER_CHANNEL_CONTEXT_MAP.get(receiveId);
        if(null == receiveChannel){
            return;
        }

        // 对两个客户端之间发送消息，接收方的联系人就是消息的发送人（创建会话时，给申请人发的ws消息则不必处理）
        if (MessageTypeEnum.ADD_FRIEND_SELF.getType().equals(messageSendDto.getMessageType())){
            // 接收人信息
            UserInfo contactUserInfo = (UserInfo) messageSendDto.getExtendData();
            messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
            messageSendDto.setContactId(contactUserInfo.getUserId());
            messageSendDto.setContactName(contactUserInfo.getNickName());
            messageSendDto.setExtendData(null);
        }else {
            messageSendDto.setContactId(messageSendDto.getSendUserId());
            messageSendDto.setContactName(messageSendDto.getSendUserNickName());
        }

        receiveChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.covertObj2Json(messageSendDto)));
    }

    /**
     * 将用户channel加入groupChannel
     * @param groupId   群组id
     * @param channel   用户channel
     */
    public void add2Group(String groupId, Channel channel){
        if (null == channel){
            return;
        }

        ChannelGroup groupChannel = GROUP_CHANNEL_CONTEXT_MAP.get(groupId);
        if (null == groupChannel){
            groupChannel = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CHANNEL_CONTEXT_MAP.put(groupId, groupChannel);
        }
        groupChannel.add(channel);
    }

    /**
     * 将用户加入到groupChannel
     * @param userId        当前用户id
     * @param groupId       群组id
     */
    public void addUser2Group(String userId, String groupId) {
        Channel channel = USER_CHANNEL_CONTEXT_MAP.get(userId);
        add2Group(groupId, channel);
    }

    /**
     * 用户离线
     * @param channel   通道
     */
    public void removeContext(Channel channel){
        // 从channel中取出userId
        String userId = channel.attr(AttributeKey.valueOf(channel.id().toString())).get().toString();

        // 从内存中移除channel
        if (!StringUtils.isEmpty(userId)){
            USER_CHANNEL_CONTEXT_MAP.remove(userId);
        }

        // 从redis中删除用户心跳
        redisComponent.removeUserHeartBeat(userId);

        // 更新用户离线时间
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setLastOffTime(System.currentTimeMillis());
        userInfoMapper.updateById(userInfo);
    }

    // 关闭用户连接（强制下线）
    public void closeContext(String userId){
        if (StringUtils.isEmpty(userId)){
            return;
        }

        // 删除redis中的token
        redisComponent.removeUserTokenByUserId(userId);

        // 关闭channel
        Channel channel = USER_CHANNEL_CONTEXT_MAP.get(userId);
        if (null == channel){
            return;
        }
        channel.close();
    }


}
