package com.example.demo.config;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Configuration
public class NettyConfig {
    //存储每一个客户端接入进来的对象
    public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static ConcurrentMap<String, ChannelId> userChannelMap=new ConcurrentHashMap();

    public  static void addChannel(String userId, Channel channel){
        group.add(channel);
        userChannelMap.put(userId,channel.id());
        log.info("新连接建立 userID {} ChannelID {}", userId, channel.id());
    }

    public static void removeChannel(String userId){
        ChannelId channelId = userChannelMap.get(userId);
        group.remove(group.find(channelId));
        userChannelMap.remove(userId);
        log.info("连接被移出 userID {} ChannelID {}", userId, channelId);
    }

    public static void removeChannel(Channel channel){
        Collection<ChannelId> values = userChannelMap.values();
        values.remove(channel.id());
        group.remove(channel);
        log.info("连接被移出 ChannelID {}", channel);
    }

    public static Channel findChannelByUserId(String userId){
        return group.find(userChannelMap.get(userId));
    }

    public static void send2All(TextWebSocketFrame tws){

        group.writeAndFlush(tws);
    }


}
