package com.mychat.service;

import com.mychat.entity.dto.MessageSendDto;
import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.entity.po.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
* @author Administrator
* @description 针对表【chat_message(聊天消息表)】的数据库操作Service
* @createDate 2025-05-14 00:00:01
*/
public interface ChatMessageService extends IService<ChatMessage> {

    /**
     * 保存用户发送的消息
     * @param chatMessage           用户消息对象
     * @param tokenUserInfoDto      用户token信息对象
     * @return MessageSendDto
     */
    MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto);

    /**
     * 上传文件
     * @param userId            当前用户id
     * @param messageId         消息id（自增id）
     * @param file              文件
     * @param cover             文件封面图
     */
    void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover);

    /**
     * 下载其他用户上传文件
     * @param tokenUserInfoDto  用户token信息对象
     * @param fileId            文件id（此处实际上是messageId）
     * @param showCover         是否下载封面
     * @return                  File对象
     */
    File downloadFile(TokenUserInfoDto tokenUserInfoDto, Long fileId, Boolean showCover);
}
