package com.mychat.websocket.netty;

import com.mychat.entity.config.AppConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * projectName: com.mychat.websocket.netty
 * author:  SoulGoodman-coder
 * description: netty启动类
 */

@Component
@Slf4j
public class NettyWebSocketStart {

    @Resource
    private HandlerHeartBeat handlerHeartBeat;

    @Resource
    private HandlerWebSocket handlerWebSocket;

    @Resource
    private AppConfig appConfig;

    // 初始化两个线程池
    private static EventLoopGroup bossGroup = new NioEventLoopGroup();
    private static EventLoopGroup workerGroup = new NioEventLoopGroup();

    @Async
    public void startNettyService() {
        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)           // 指定上面创建的两个线程池
                    .channel(NioServerSocketChannel.class)          // 指定通道类型
                    .handler(new LoggingHandler(LogLevel.DEBUG))    // 添加日志处理器，指定日志级别
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 设置几个重要处理器
                            socketChannel.pipeline().addLast(new HttpServerCodec())         // 添加http编码、解码器
                                    .addLast(new HttpObjectAggregator(64 * 1024))    // 聚合解码：用于将HTTP请求或响应的多个部分聚合成完整的FullHttpRequest或FullHttpResponse
                                    .addLast(new IdleStateHandler(6, 0, 0, TimeUnit.SECONDS))        // 心跳检测（接收心跳超时时间, 发送心跳超时时间, 所有类型超时时间, 时间参数单位）
                                    .addLast(handlerHeartBeat)        // 自定义心跳超时处理器
                                    .addLast(new WebSocketServerProtocolHandler("/ws", null, true, 64*1024, true, true, 10000)) // 添加websocket处理器
                                    .addLast(handlerWebSocket);        // 自定义消息处理器
                        }
                    });

            // 从JVM运行参数中获取ws端口号（若启动时未设置，则从appConfig中获取）
            Integer wsPort = Integer.parseInt(System.getProperty("ws.port", appConfig.getWsPort().toString()));

            ChannelFuture channelFuture = serverBootstrap.bind(wsPort).sync();
            log.info("启动netty成功，端口：{}", wsPort);
            channelFuture.channel().closeFuture().sync();

        }catch (Exception e) {
            log.error("启动netty失败", e);
        }finally {
            // 关闭线程池
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @PreDestroy
    private void stopNettyService() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
