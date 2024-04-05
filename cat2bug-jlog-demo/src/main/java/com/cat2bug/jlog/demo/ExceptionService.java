package com.cat2bug.jlog.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;

/**
 * @Author: yuzhantao
 * @CreateTime: 2024-04-02 17:44
 * @Version: 1.0.0
 */
@Service
public class ExceptionService {
    private static final Logger log = LoggerFactory.getLogger(ExceptionService.class);
    @Scheduled(cron = "0/1 * * * * *")
    public void testException() throws Exception {
        if(Math.random()>0.6) {
            throw new RuntimeException("测试一个异常");
        } else if(Math.random()>0.7) {
            throw new IOException();
        } else if(Math.random()>0.8) {
            throw new OutOfMemoryError();
        } else if(Math.random()>0.9) {
            throw new ParseException("测试格式转换错误",1);
        } else {
            log.info("本次触发没有创建异常");
        }
    }
}
