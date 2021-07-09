package com.example.demo.handler;

import io.netty.channel.ChannelInitializer;
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

    final LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);

    @Override
    protected void initChannel(SocketChannel e) throws Exception {

        //添加具体的handler
        e.pipeline().addLast("logging", loggingHandler); //for better debug
        e.pipeline().addLast("http-codec",new HttpServerCodec());  // for http protocol. used for shake hands
        e.pipeline().addLast("aggregator",new HttpObjectAggregator(65536)); // 将多个消息转换为单一的FullHttpRequest或FullHttpResponse对象
        e.pipeline().addLast("http-chunked",new ChunkedWriteHandler()); //是支持异步发送大的码流,但不占用过多的内存,防止JAVA内存溢出



        //========================增加心跳支持 start    ========================

        //针对客户端，如果在1分钟时没有想服务端发送写心跳(ALL)，则主动断开
        //如果是读空闲或者写空闲，不处理
        //e.pipeline().addLast(new IdleStateHandler(8, 10, 12));

        //自定义的空闲检测
        //e.pipeline().addLast(new HeartBeatHandler());
        //========================增加心跳支持 end      ========================

        //e.pipeline().addLast(new WebSocketServerCompressionHandler());
        //e.pipeline().addLast(new WebSocketServerProtocolHandler("/webSocket", null, true));  //websocket支持,设置路由
        e.pipeline().addLast("handler",new MyWebSocketHandler());  //自定义的handler

        //执行时间较长的Handler 建议另外创建一个EventLoopGroup 来（额外的线程）单独处理，避免该handler影响其他线程执行效率和时间
//        EventLoopGroup group = new DefaultEventLoop();
//        e.pipeline().addLast(group, "long-time-handler", new ChannelInboundHandlerAdapter() {
//            @Override
//            public void channelRead(ChannelHandlerContext ctx, Object msg) {
//                ByteBuf buf = (ByteBuf) msg;
//                ctx.fireChannelRead(msg); // 让消息传递给下一个handler(如果有的话)
//            }
//        });
    }
}
