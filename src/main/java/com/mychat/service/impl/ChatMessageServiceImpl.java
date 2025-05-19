package com.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mychat.entity.config.AppConfig;
import com.mychat.entity.dto.MessageSendDto;
import com.mychat.entity.dto.SysSettingDto;
import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.entity.po.ChatMessage;
import com.mychat.entity.po.ChatSession;
import com.mychat.entity.po.UserContact;
import com.mychat.exception.BusinessException;
import com.mychat.mapper.ChatSessionMapper;
import com.mychat.mapper.UserContactMapper;
import com.mychat.redis.RedisComponent;
import com.mychat.service.ChatMessageService;
import com.mychat.mapper.ChatMessageMapper;
import com.mychat.utils.CopyUtils;
import com.mychat.utils.StringUtils;
import com.mychat.utils.enums.*;
import com.mychat.websocket.MessageHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

/**
* @author Administrator
* @description 针对表【chat_message(聊天消息表)】的数据库操作Service实现
* @createDate 2025-05-14 00:00:01
*/
@Service
@Slf4j
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService{

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private AppConfig appConfig;

    @Value("#{'${contants.IMAGE_SUFFIX_LIST}'.split(',')}")
    private List<String> IMAGE_SUFFIX_LIST;

    @Value("#{'${contants.VIDEO_SUFFIX_LIST}'.split(',')}")
    private List<String> VIDEO_SUFFIX_LIST;

    @Value("${contants.FILE_FOLDER_FILE}")
    private String FILE_FOLDER_FILE;

    @Value("${contants.COVER_PNG_SUFFIX}")
    private String COVER_PNG_SUFFIX;


    /**
     * 保存用户发送的消息
     *
     * @param chatMessage      用户消息对象
     * @param tokenUserInfoDto 用户token信息对象
     * @return MessageSendDto
     */
    @Override
    public MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto) {
        // 若该条消息不是机器人消息，则判断其好友状态
        SysSettingDto settingDto = new SysSettingDto();
        if (!settingDto.getRobotUid().equals(tokenUserInfoDto.getUserId())){
            // 用户联系人列表
            List<String> userContactIdList = redisComponent.getUserContactIdList(tokenUserInfoDto.getUserId());
            // 消息的接收人id不在用户联系人列表中
            if (!userContactIdList.contains(chatMessage.getContactId())){
                UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(chatMessage.getContactId());
                if (UserContactTypeEnum.USER == typeEnum){
                    throw new BusinessException(ResultCodeEnum.CODE_902);
                }else {
                    throw new BusinessException(ResultCodeEnum.CODE_903);
                }
            }
        }

        String sessionId = null;                                                                        // 会话id
        String sendUserId = tokenUserInfoDto.getUserId();                                               // 发送人id
        String sendUserNickName = tokenUserInfoDto.getNickName();                                       // 发送人昵称
        String contactId = chatMessage.getContactId();                                                  // 接收人id
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);               // 接收人类型（人|群）
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(chatMessage.getMessageType());      // 消息类型

        // 只允许普通聊天消息和媒体文件消息类型的消息
        if (null == messageTypeEnum || !ArrayUtils.contains(new Integer[]{MessageTypeEnum.CHAT.getType(), MessageTypeEnum.MEDIA_CHAT.getType()}, messageTypeEnum.getType())) {
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        // 获取sessionId
        if (UserContactTypeEnum.USER == contactTypeEnum){
            sessionId = StringUtils.getChatSessionId4User(new String[]{sendUserId, contactId});
        }else {
            sessionId = StringUtils.getChatSessionId4Group(contactId);
        }

        // 消息状态（若是媒体类型消息则为发送中状态，若是普通消息则为已发送状态）
        Integer status = MessageTypeEnum.MEDIA_CHAT == messageTypeEnum? MessageStatusEnum.SENDING.getStatus() : MessageStatusEnum.SENDED.getStatus();

        // 处理消息内容
        String messageContact = StringUtils.clearHtmlTag(chatMessage.getMessageContent());

        // 补全chatMessage对象属性
        long curTime = System.currentTimeMillis();
        chatMessage.setSessionId(sessionId);
        chatMessage.setSendTime(curTime);
        chatMessage.setSendUserId(sendUserId);
        chatMessage.setSendUserNickName(sendUserNickName);
        chatMessage.setStatus(status);
        chatMessage.setMessageContent(messageContact);
        chatMessage.setContactType(contactTypeEnum.getType());

        // 更新聊天消息表 chatMessage
        chatMessageMapper.insert(chatMessage);

        // 更新会话信息表 chatSession
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastMessage(messageContact);
        if (UserContactTypeEnum.GROUP == contactTypeEnum){
            // 若是群聊消息，则在消息前添加发送人信息
            chatSession.setLastMessage(sendUserId + ": " + contactId);
        }
        chatSession.setLastReceiveTime(curTime);
        chatSessionMapper.updateById(chatSession);

        /*
        * 发送ws消息
        * */
        MessageSendDto messageSendDto = CopyUtils.copy(chatMessage, MessageSendDto.class);
        // 若是跟机器人聊天(封装机器人回复信息对象和机器人token对象，再次调用saveMessage方法完成机器人回复消息)
        if (settingDto.getRobotUid().equals(contactId)){
            TokenUserInfoDto robotTokenUserInfoDto = new TokenUserInfoDto();
            robotTokenUserInfoDto.setUserId(settingDto.getRobotUid());
            robotTokenUserInfoDto.setNickName(settingDto.getRobotNickName());

            ChatMessage robotChatMessage = new ChatMessage();
            // 机器人回复的消息联系人为当前用户id
            robotChatMessage.setContactId(sendUserId);
            // TODO 对接AI，回复消息
            robotChatMessage.setMessageContent("机器人聊天功能开发中......");
            robotChatMessage.setMessageType(MessageTypeEnum.CHAT.getType());
            saveMessage(robotChatMessage, robotTokenUserInfoDto);
        }else {
            messageHandler.sendMessage(messageSendDto);
        }

        return messageSendDto;
    }

    /**
     * 上传文件
     *
     * @param userId    当前用户id
     * @param messageId 消息id（自增id）
     * @param file      文件
     * @param cover     文件封面图
     */
    @Override
    public void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover) {
        ChatMessage chatMessage = chatMessageMapper.selectById(messageId);
        // 校验参数是否合法
        if (null == chatMessage || !chatMessage.getSendUserId().equals(userId)){
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        // 校验文件大小是否超出
        SysSettingDto settingDto = new SysSettingDto();
        String fileName = file.getOriginalFilename();               // 文件名
        String fileSuffix = StringUtils.getFileSuffix(fileName);    // 文件类型后缀（.png .mp4等）
        if (
                !StringUtils.isEmpty(fileSuffix)
                && IMAGE_SUFFIX_LIST.contains(fileSuffix.toLowerCase())
                // file.getSize()获取的是字节大小，settingDto中的是MB，要进行转化
                && file.getSize() > settingDto.getMaxImageSize() * 1024 * 1024
        ) {
            throw new BusinessException(ResultCodeEnum.CODE_600);
        } else if (
                !StringUtils.isEmpty(fileSuffix)
                && VIDEO_SUFFIX_LIST.contains(fileSuffix.toLowerCase())
                // file.getSize()获取的是字节大小，settingDto中的是MB，要进行转化
                && file.getSize() > settingDto.getMaxVideoSize() * 1024 * 1024
        ) {
            throw new BusinessException(ResultCodeEnum.CODE_600);
        } else if (!StringUtils.isEmpty(fileSuffix)
                && !IMAGE_SUFFIX_LIST.contains(fileSuffix.toLowerCase())
                && !VIDEO_SUFFIX_LIST.contains(fileSuffix.toLowerCase())
                // file.getSize()获取的是字节大小，settingDto中的是MB，要进行转化
                && file.getSize() > settingDto.getMaxFileSize() * 1024 * 1024
        ) {
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }

        // 重命名文件为 messageId + 后缀
        String fileRealName = messageId + fileSuffix;

        /*
        * 构建文件保存目录
        * */
        // 年月（202505）
        SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
        String month = format.format(chatMessage.getSendTime());
        // 文件存储目录（不存在则创建）
        File folder = new File(appConfig.getProjectFolder() + FILE_FOLDER_FILE + month);
        if (!folder.exists()){
            folder.mkdirs();
        }
        // 文件上传的完整路径 （如：D:/MyChat/file/202505/2335.png）
        File uploadFile = new File(folder.getPath() + "/" + fileRealName);

        // 保存文件到服务器
        try {
            System.out.println("uploadFile.getPath() = " + uploadFile.getPath());
            file.transferTo(uploadFile);
            cover.transferTo(new File(uploadFile.getPath() + COVER_PNG_SUFFIX));
        }catch (Exception e){
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }

        // 更新chatMessage表中消息状态
        LambdaUpdateWrapper<ChatMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatMessage::getMessageId, messageId)
                     .eq(ChatMessage::getStatus, MessageStatusEnum.SENDING.getStatus())
                     .set(ChatMessage::getStatus, MessageStatusEnum.SENDED.getStatus());
        chatMessageMapper.update(updateWrapper);

        // 给文件接收方发送ws消息，通知文件已上传服务器
        MessageSendDto<Object> messageSendDto = new MessageSendDto<>();
        messageSendDto.setMessageId(messageId);
        messageSendDto.setStatus(MessageStatusEnum.SENDED.getStatus());
        messageSendDto.setMessageType(MessageTypeEnum.FILE_UPLOAD.getType());
        messageSendDto.setContactId(chatMessage.getContactId());
        messageHandler.sendMessage(messageSendDto);
    }

    /**
     * 下载其他用户上传文件
     *
     * @param tokenUserInfoDto 用户token信息对象
     * @param fileId           文件id（此处实际上是messageId）
     * @param showCover        是否下载封面
     * @return File对象
     */
    @Override
    public File downloadFile(TokenUserInfoDto tokenUserInfoDto, Long fileId, Boolean showCover) {
        ChatMessage chatMessage = chatMessageMapper.selectById(fileId);
        // 校验参数是否合法
        if (null == chatMessage){
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }
        String contactId = chatMessage.getContactId();  // 联系人id
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);   // 联系人类型
        // 判断当前用户是否是文件消息的接收人
        if (UserContactTypeEnum.USER == contactTypeEnum && !tokenUserInfoDto.getUserId().equals(contactId)){
            throw new BusinessException(ResultCodeEnum.CODE_600);
        }
        // 判断当前用户是否在群组中
        if (UserContactTypeEnum.GROUP == contactTypeEnum){
            LambdaQueryWrapper<UserContact> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserContact::getUserId, tokenUserInfoDto.getUserId())
                        .eq(UserContact::getContactType, UserContactTypeEnum.GROUP.getType())
                        .eq(UserContact::getContactId, contactId)
                        .eq(UserContact::getStatus, UserContactStatusEnum.FRIEND.getStatus());
            Long count = userContactMapper.selectCount(queryWrapper);
            if (count == 0){
                throw new BusinessException(ResultCodeEnum.CODE_600);
            }
        }

        /*
         * 构建文件保存目录
         * */
        // 年月（202505）
        SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
        String month = format.format(chatMessage.getSendTime());
        // 文件存储目录
        File folder = new File(appConfig.getProjectFolder() + FILE_FOLDER_FILE + month);

        // 文件名
        String fileRealName = fileId + StringUtils.getFileSuffix(chatMessage.getFileName());
        if (null != showCover && showCover) {
            fileRealName = fileRealName + COVER_PNG_SUFFIX;
        }

        // 文件在服务器上的完整路径 （如：D:/MyChat/file/202505/2335.png）
        File file = new File(folder.getPath() + "/" + fileRealName);

        if (!file.exists()){
            log.error("文件不存在,messageId={}", fileId);
            throw new BusinessException(ResultCodeEnum.CODE_602);
        }

        return file;
    }
}




