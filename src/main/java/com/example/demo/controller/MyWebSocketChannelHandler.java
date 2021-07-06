package com.example.demo.controller;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 初始化链接时候的组件
 */
public class MyWebSocketChannelHandler extends ChannelInitializer<SocketChannel>{

    @Override
    protected void initChannel(SocketChannel e) throws Exception {

        //添加具体的handler
        e.pipeline().addLast("logging", new LoggingHandler(LogLevel.INFO)); //for better debug
        e.pipeline().addLast("http-codec",new HttpServerCodec());
        e.pipeline().addLast("aggregator",new HttpObjectAggregator(65536));
        e.pipeline().addLast("http-chunked",new ChunkedWriteHandler());
        e.pipeline().addLast("handler",new MyWebSockeHandler());  //自定义的handler

        //执行时间较长的Handler 建议另外创建一个EventLoopGroup 来（额外的线程）单独处理，避免该handler影响其他线程执行效率和时间
        EventLoopGroup group = new DefaultEventLoop();
        e.pipeline().addLast(group, "long-time-handler", new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                ByteBuf buf = (ByteBuf) msg;
                ctx.fireChannelRead(msg); // 让消息传递给下一个handler(如果有的话)
            }
        });
    }
}
