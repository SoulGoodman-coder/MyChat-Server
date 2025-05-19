package com.mychat.websocket;

import com.mychat.entity.dto.MessageSendDto;
import com.mychat.utils.JsonUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * projectName: com.mychat.websocket
 * author:  SoulGoodman-coder
 * description: 消息处理器
 */

@Component("messageHandler")
@Slf4j
public class MessageHandler {
    private static final String MESSAGE_TOPIC = "message.topic";

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ChannelContextUtils channelContextUtils;

    // 添加redis消息监听器
    @PostConstruct
    public void listenMessage() {
        // 订阅redis消息，addListener(要监听的消息类型)
        redissonClient.getTopic(MESSAGE_TOPIC).addListener(MessageSendDto.class, (MessageSendDto, sendDto) -> {
            log.info("收到广播消息：{}", JsonUtils.covertObj2Json(sendDto));
            channelContextUtils.sendMessage(sendDto);
        });
    }

    // 发布redis消息
    public void sendMessage(MessageSendDto sendDto) {
        redissonClient.getTopic(MESSAGE_TOPIC).publish(sendDto);
    }
}
