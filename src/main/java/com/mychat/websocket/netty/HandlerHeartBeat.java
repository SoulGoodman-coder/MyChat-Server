package com.mychat.websocket.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * projectName: com.mychat.websocket.netty
 * author:  SoulGoodman-coder
 * description: 自定义心跳超时处理器
 * ChannelDuplexHandler是Netty提供的一个处理器接口，它继承了ChannelInboundHandler和ChannelOutboundHandler两个接口。
 * 因此，实现了ChannelDuplexHandler接口的类可以在同一个处理器中同时处理入站事件（如读操作）和出站事件（如写操作、连接、绑定等）
 */

@ChannelHandler.Sharable
@Component
@Slf4j
public class HandlerHeartBeat extends ChannelDuplexHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (IdleState.READER_IDLE == event.state()) {
                Channel channel = ctx.channel();
                // 获取用户的id
                String userId = channel.attr(AttributeKey.valueOf(channel.id().toString())).toString();
                log.info("{}心跳超时", userId);
                channel.close();   // 心跳超时则关闭通道连接
            }else if (IdleState.WRITER_IDLE == event.state()) {
                ctx.writeAndFlush("heart");
            }

        }
    }
}
