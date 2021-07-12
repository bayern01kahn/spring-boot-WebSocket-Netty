package com.example.demo.handler;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Justin.Luo
 */
@Slf4j
public class ServerBusinessProcessor extends AppBusinessProcessor {

    public void process(Object message) {
        log.info("服务端执行业务处理..."+ message);

        // TODO: biz goes here

    }
}
