package com.example.demo.handler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RestController
public class StartWebSocket {

//    @GetMapping(value = "/action/webSocket")
//    public static void action(){
//        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(); //Boss 4 Accept Event
//        EventLoopGroup workGroup = new NioEventLoopGroup();      //Worker 4 Read/Write Event
//        try {
//            //1.开启服务端
//            ServerBootstrap serverBootstrap = new ServerBootstrap();
//
//            //2.设置group (BossEventGroup[负责处理连接]， WorkerEventGroup[负责处理读写])
//            serverBootstrap.group(eventLoopGroup,workGroup);
//
//            //3.选择 服务器的ServerSocketChannel实现类
//            serverBootstrap.channel(NioServerSocketChannel.class);
//
//            //4.设置负责处理这些作为worker的能做哪些事情[编解码,数据读写等]，这里使用自定义的处理器
//            serverBootstrap.childHandler(new MyWebSocketChannelHandler());
//            System.out.println("服务端开启等待客户端连接..");
//            Channel channel = serverBootstrap.bind(8888)
//                    .sync()   // 阻塞方法，直到连接建立
//                    .channel(); // 拿到 连接对象
//            channel.closeFuture().sync();
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
//            //退出程序
//            eventLoopGroup.shutdownGracefully();
//            workGroup.shutdownGracefully();
//        }
//    }
}
