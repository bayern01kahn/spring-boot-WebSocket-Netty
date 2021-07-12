package com.example.demo.config;

import com.example.demo.handler.MyWebSocketChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Author: Justin.Luo
 */
@Component
public class NettyServer {

    @PostConstruct
    private void autoStart() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); //Boss 4 Accept Event
        EventLoopGroup workGroup = new NioEventLoopGroup(); //Worker 4 Read/Write Event
        try {
            //1.开启服务端
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            //2.设置group (BossEventGroup[负责处理连接]， WorkerEventGroup[负责处理读写])
            serverBootstrap.group(bossGroup,workGroup)
                           .channel(NioServerSocketChannel.class) //3.选择 服务器的ServerSocketChannel实现类
                           //.handler()
                           //.option(ChannelOption.SO_BACKLOG, 1024)  // [用于设置Boss线程组] 保持连接数
                           //.option(ChannelOption.TCP_NODELAY, true) // 有数据立即发送

                           //.option(ChannelOption.SO_KEEPALIVE, true); // 2小时无数据激活心跳机制
                           .childHandler(new MyWebSocketChannelHandler()); //4.设置负责处理这些作为worker的能做哪些事情[编解码,数据读写等]，这里使用自定义的处理器
                           //.childOption(ChannelOption.SO_KEEPALIVE, true)  // [用于设置Worker 线程组] 保持连接

            System.out.println("服务端开启等待客户端连接..");

            Channel channel = serverBootstrap.bind(8888)
                    .sync()   // 阻塞方法，直到连接建立
                    .channel(); // 拿到 连接对象
            channel.closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //退出程序
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
