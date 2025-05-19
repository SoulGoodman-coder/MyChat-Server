package com.mychat.websocket.netty;

import com.mychat.entity.dto.TokenUserInfoDto;
import com.mychat.redis.RedisComponent;
import com.mychat.utils.StringUtils;
import com.mychat.websocket.ChannelContextUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * projectName: com.mychat.websocket.netty
 * author:  SoulGoodman-coder
 * description: 自定义webSocket处理类
 */

@ChannelHandler.Sharable
@Component
@Slf4j
public class HandlerWebSocket extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ChannelContextUtils channelContextUtils;

    // 通道就绪时，该方法被调用，一般用作初始化
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("----------------有新的连接{}加入----------------", ctx.channel().id().asLongText());
    }

    // 通道断开时，该方法被调用
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channelContextUtils.removeContext(ctx.channel());
        log.info("----------------有连接{}断开----------------", ctx.channel().id().asLongText());
    }

    // 处理接收到的数据
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        Channel channel = ctx.channel();
        String userId = channel.attr(AttributeKey.valueOf(channel.id().toString())).get().toString();
        log.info("接收到{}消息{}", userId, msg.text());

        // 更新redis用户心跳
        redisComponent.saveUserHeartBeat(userId);

    }

    // 客户端连接时触发该方法
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {

            // 身份校验
            WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            String uri = complete.requestUri();
            log.info("uri: {}", uri);
            // 解析token
            String token = getToken(uri);
            log.info("token: {}", token);
            if (null == token){
                ctx.channel().close();      //关闭通道连接
                return;
            }

            TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(token);
            if (null == tokenUserInfoDto){
                ctx.channel().close();      //关闭通道连接
                return;
            }

            channelContextUtils.addContext(tokenUserInfoDto.getUserId(), ctx.channel());
        }
    }

    /**
     * 从uri中解析出token字符串
     * @param uri   uri
     * @return      token字符串
     */
    private String getToken(String uri){
        // 判断uri是否合法
        if (StringUtils.isEmpty(uri) || !uri.startsWith("/") || !uri.contains("?")) {
            return null;
        }
        String[] params1 = uri.split("\\?");
        if (params1.length != 2) {
            return null;
        }
        String[] params2 = params1[1].split("=");
        if (params2.length != 2) {
            return null;
        }

        return params2[1];
    }
}
