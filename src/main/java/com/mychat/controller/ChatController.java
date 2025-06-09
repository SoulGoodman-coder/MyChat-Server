package com.mychat.controller;

import com.mychat.annotation.GlobalInterceptor;
import com.mychat.entity.config.AppConfig;
import com.mychat.entity.dto.MessageSendDto;
import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.entity.po.ChatMessage;
import com.mychat.exception.BusinessException;
import com.mychat.service.ChatMessageService;
import com.mychat.utils.Result;
import com.mychat.utils.StringUtils;
import com.mychat.utils.enums.ResultCodeEnum;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

/**
 * projectName: com.mychat.controller
 * author:  SoulGoodman-coder
 * description: 聊天模块controller
 */

@RestController("chatController")
@RequestMapping("chat")
@Validated      // 参数校验
@Slf4j
public class ChatController extends BaseController{

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private AppConfig appConfig;

    @Value("${contants.FILE_FOLDER_FILE}")
    private String FILE_FOLDER_FILE;

    @Value("${contants.FILE_FOLDER_AVATAR_NAME}")
    private String FILE_FOLDER_AVATAR_NAME;

    @Value("${contants.PNG_SUFFIX}")
    private String PNG_SUFFIX;

    @Value("${contants.COVER_PNG_SUFFIX}")
    private String COVER_PNG_SUFFIX;

    /**
     * 发送消息
     * @param request           request
     * @param contactId         联系人id
     * @param messageContent    消息内容（最多500字）
     * @param messageType       消息类型 2：普通消息 5：媒体消息
     * @param fileSize          文件大小
     * @param fileName          文件名称
     * @param fileType          文件类型 0：图片 1：视频
     * @return Result
     */
    @RequestMapping("sendMessage")
    @GlobalInterceptor
    public Result sendMessage(HttpServletRequest request,
                              @NotBlank String contactId,
                              @Size(max = 500) @NotBlank String messageContent,
                              @NotNull Integer messageType,
                              Long fileSize,
                              String fileName,
                              Integer fileType){

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContactId(contactId);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setMessageType(messageType);
        chatMessage.setFileSize(fileSize);
        chatMessage.setFileName(fileName);
        chatMessage.setFileType(fileType);
        MessageSendDto messageSendDto = chatMessageService.saveMessage(chatMessage, tokenUserInfoDto);

        return Result.ok(messageSendDto);
    }

    /**
     * 上传文件
     * @param request       request
     * @param messageId     消息id（自增id）
     * @param file          文件
     * @param cover         文件封面图
     * @return Result
     */
    @RequestMapping("uploadFile")
    @GlobalInterceptor
    public Result uploadFile(HttpServletRequest request,
                             @NotNull Long messageId,
                             @NotNull MultipartFile file,
                             @NotNull MultipartFile cover){

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        chatMessageService.saveMessageFile(tokenUserInfoDto.getUserId(), messageId, file, cover);
        return Result.ok(null);
    }

    /**
     * 下载文件
     * @param request       request
     * @param response      response
     * @param fileId        文件id（userId或groupId或sessionId）
     * @param showCover     是否获取封面
     */
    @RequestMapping("downloadFile")
    @GlobalInterceptor
    public void downloadFile(HttpServletRequest request,
                             HttpServletResponse response,
                             @NotBlank String fileId,
                             @NotNull Boolean showCover){

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        OutputStream out = null;
        FileInputStream in = null;

        try {
            File file = null;
            if(!StringUtils.isNumber(fileId)){
                /*
                * 下载头像文件
                * */
                // 文件完整路径
                String avatarPath = appConfig.getProjectFolder() + FILE_FOLDER_FILE + FILE_FOLDER_AVATAR_NAME + fileId + PNG_SUFFIX;
                if (showCover){
                    avatarPath = avatarPath + COVER_PNG_SUFFIX;
                }
                file = new File(avatarPath);
                if (!file.exists()) {
                    throw new BusinessException(ResultCodeEnum.CODE_602);
                }

            }else {
                /*
                 * 下载用户发送文件
                 * */
                file = chatMessageService.downloadFile(tokenUserInfoDto, Long.valueOf(fileId), showCover);
            }
            response.setContentType("application/x-msdownload;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String((fileId + PNG_SUFFIX).getBytes(), "ISO-8859-1"));
            response.setContentLengthLong(file.length());
            in = new FileInputStream(file);
            byte[] byteDate = new byte[1024];
            out = response.getOutputStream();
            int len = 0;
            while ((len = in.read(byteDate)) != -1){
                out.write(byteDate, 0, len);
            }
            out.flush();

        }catch (Exception e){
            log.error("文件下载失败", e);
        }finally {
            // 关闭流
            if (null != out){
                try {
                    out.close();
                }catch (Exception e){
                    log.error("IO异常", e);
                }
            }
            if (null != in){
                try {
                    in.close();
                }catch (Exception e){
                    log.error("IO异常", e);
                }
            }

        }
    }

}
