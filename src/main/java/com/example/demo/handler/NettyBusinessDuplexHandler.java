package com.example.demo.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Justin.Luo
 */
@Slf4j
public class NettyBusinessDuplexHandler extends ChannelDuplexHandler {

    private AppBusinessProcessor bizProcessor = null;

    public NettyBusinessDuplexHandler(AppBusinessProcessor appBizHandler) {
        super();
        this.bizProcessor = appBizHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

            // 处理业务消息
            log.info("收到消息  -- {}", msg.toString());
            bizProcessor.process(msg);
            // 如果接收到的是请求，则需要写回响应消息

        // 继续传递给Pipeline下一个Handler
        // super.channelRead(ctx, msg);
        // ctx.fireChannelRead(msg);
    }
}