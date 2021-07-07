package com.example.demo.config;

import com.example.demo.controller.MyWebSocketChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Configuration
public class NettyConfig {
    //存储每一个客户端接入进来的对象
    public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private  static ConcurrentMap<String, ChannelId> ChannelMap=new ConcurrentHashMap();

    public  static void addChannel(Channel channel){
        group.add(channel);
        ChannelMap.put(channel.id().asShortText(),channel.id());
        log.info("新连接建立 ID-shortText {} ChannelID {}", channel.id().asShortText(), channel.id());
    }

    public static void removeChannel(Channel channel){
        group.remove(channel);
        ChannelMap.remove(channel.id().asShortText());
        log.info("连接被移出 ID-shortText {} ChannelID {}", channel.id().asShortText(), channel.id());
    }

    public static  Channel findChannel(String id){
        return group.find(ChannelMap.get(id));
    }

    public static void send2All(TextWebSocketFrame tws){
        group.writeAndFlush(tws);
    }

    @PostConstruct
    private void autoStart() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(); //Boss 4 Accept Event
        EventLoopGroup workGroup = new NioEventLoopGroup();      //Worker 4 Read/Write Event
        try {
            //1.开启服务端
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            //2.设置group (BossEventGroup[负责处理连接]， WorkerEventGroup[负责处理读写])
            serverBootstrap.group(eventLoopGroup,workGroup);

            //3.选择 服务器的ServerSocketChannel实现类
            serverBootstrap.channel(NioServerSocketChannel.class);

            //4.设置负责处理这些作为worker的能做哪些事情[编解码,数据读写等]，这里使用自定义的处理器
            serverBootstrap.childHandler(new MyWebSocketChannelHandler());
            System.out.println("服务端开启等待客户端连接..");
            Channel channel = serverBootstrap.bind(8888)
                    .sync()   // 阻塞方法，直到连接建立
                    .channel(); // 拿到 连接对象
            channel.closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //退出程序
            eventLoopGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
