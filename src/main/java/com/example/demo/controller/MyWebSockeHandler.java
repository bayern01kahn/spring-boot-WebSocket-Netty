package com.example.demo.controller;

import com.example.demo.config.NettyConfig;
import com.example.demo.utils.RequestUriUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.example.demo.config.NettyConfig.send2All;

/**
 * 接受/处理/响应客户端websocke请求的核心业务处理类
 */
@Slf4j
public class MyWebSockeHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketServerHandshaker webSocketServerHandshaker;
    private static final String WEB_SOCKET_URL = "ws://localhost:8888/webSocket";


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        //System.out.println("channel注册");
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        //System.out.println("channel移除");
        super.channelUnregistered(ctx);
    }

    //客户端与服务端创建链接的时候调用
    @Override
    public void channelActive (ChannelHandlerContext context)throws Exception{
        NettyConfig.addChannel(context.channel());
        System.out.println("客户端与服务端连接开启: "+ context.channel().remoteAddress().toString());
    }
    //客户端与服务端断开连接的时候调用
    @Override
    public void channelInactive(ChannelHandlerContext context)throws Exception{
        NettyConfig.removeChannel(context.channel());
        System.out.println("客户端与服务端连接断开 "+ context.channel().remoteAddress().toString());
    }
    //服务端接收客户端发送过来的数据结束之后调用
    @Override
    public void channelReadComplete(ChannelHandlerContext context)throws Exception{
        //System.out.println("channel读取数据完毕");
        //super.channelReadComplete(context);
        context.flush();
    }
    //工程出现异常的时候调用
    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable throwable)throws Exception{
        throwable.printStackTrace();
        context.close();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("助手类添加");
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("助手类移除");
        super.handlerRemoved(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("channel用户事件触发");
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel可写更改");
        super.channelWritabilityChanged(ctx);
    }

    //服务端处理客户端websocke请求的核心方法
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        if (o instanceof TextWebSocketFrame) { // 此处仅处理 Text Frame
            String request = ((TextWebSocketFrame) o).text();
            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame("收到: " + request));
        }


        //处理客户端向服务端发起的http握手请求
        if (o instanceof FullHttpRequest){
            handHttpRequest(channelHandlerContext,(FullHttpRequest) o);
        }else if (o instanceof WebSocketFrame){//处理websocket链接业务
            handWebSocketFrame(channelHandlerContext,(WebSocketFrame) o);
        }
    }

    /**
     * 处理客户端与服务端之间的websocket业务
     * @param context
     * @param webSocketFrame
     */
    private void handWebSocketFrame(ChannelHandlerContext context,WebSocketFrame webSocketFrame){
        if (webSocketFrame instanceof CloseWebSocketFrame){//判断是否是关闭websocket的指令
            webSocketServerHandshaker.close(context.channel(),(CloseWebSocketFrame) webSocketFrame.retain());
        }
        if (webSocketFrame instanceof PingWebSocketFrame){//判断是否是ping消息
            context.channel().write(new PongWebSocketFrame(webSocketFrame.content().retain()));
            return;
        }
        if (!(webSocketFrame instanceof TextWebSocketFrame)){//判断是否是二进制消息
            System.out.println("不支持二进制消息");
            throw new RuntimeException(this.getClass().getName());
        }
        //返回应答消息
        //获取客户端向服务端发送的消息
        String request = ((TextWebSocketFrame) webSocketFrame ).text();
        System.out.println("服务端收到客户端的消息：" + request);
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(context.channel().id() + ":" + request);
        //服务端向每个连接上来的客户端发送消息
        //NettyConfig.group.writeAndFlush(textWebSocketFrame);
        NettyConfig.send2All(textWebSocketFrame);

        //send to related user
        //NettyConfig.group.find(context.channel().id()).writeAndFlush(textWebSocketFrame);
    }




    /**
     * 唯一一次的 http 请求， 用于升级为 websocket
     * 处理客户端向服务端发起http握手请求业务
     * @param context
     * @param fullHttpRequest
     */
    private void handHttpRequest(ChannelHandlerContext context,FullHttpRequest fullHttpRequest){
        String uri = fullHttpRequest.uri();
        Map<String, String> params = RequestUriUtils.getParams(uri);
        log.debug("客户端请求参数：{}", params);


        if (!fullHttpRequest.decoderResult().isSuccess() ||
                !("websocket".equals(fullHttpRequest.headers().get("Upgrade")))){//要求Upgrade为websocket，过滤掉get/Post

            //若不是websocket方式，则创建BAD_REQUEST的req，返回给客户端
            sendHttpResponse(context,fullHttpRequest,
                    new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        WebSocketServerHandshakerFactory webSocketServerHandshakerFactory = new WebSocketServerHandshakerFactory(WEB_SOCKET_URL,null,false);
        webSocketServerHandshaker = webSocketServerHandshakerFactory.newHandshaker(fullHttpRequest);
        if (webSocketServerHandshaker == null){
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(context.channel());
        }else{
            webSocketServerHandshaker.handshake(context.channel(),fullHttpRequest);
        }
    }

    /**
     * 服务端想客户端发送响应消息
     * @param context
     * @param fullHttpRequest
     * @param defaultFullHttpResponse
     */
    private void sendHttpResponse(ChannelHandlerContext context,
                                  FullHttpRequest fullHttpRequest, DefaultFullHttpResponse defaultFullHttpResponse){
        if (defaultFullHttpResponse.status().code() != 200){
            ByteBuf buf = Unpooled.copiedBuffer(defaultFullHttpResponse.getStatus().toString(), CharsetUtil.UTF_8);
            defaultFullHttpResponse.content().writeBytes(buf);
            buf.release();
        }
        //服务端向客户端发送数据
        ChannelFuture future = context.channel().writeAndFlush(defaultFullHttpResponse);
        if (defaultFullHttpResponse.status().code() !=200){  //如果是非Keep-Alive，关闭连接
            future.addListener(ChannelFutureListener.CLOSE);
        }

    }

}
