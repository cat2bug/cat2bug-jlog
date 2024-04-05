package com.cat2bug.jlog.exception;

/**
 * 推送异常
 */
public class PushException extends RuntimeException {
    public PushException(String msg){
        super(msg);
    }
}
