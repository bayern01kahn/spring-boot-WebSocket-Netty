package com.example.demo.handler;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Justin.Luo
 */
@Slf4j
public abstract class AppBusinessProcessor {

    /**
     * 执行业务处理，参数是包含的是请求消息，处理完毕后，将响应消息设置到message对象中
     *
     * @param message
     *            请求/响应消息载体
     */
    public abstract void process(Object message);
}
