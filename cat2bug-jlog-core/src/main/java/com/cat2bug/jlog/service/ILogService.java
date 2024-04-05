package com.cat2bug.jlog.service;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.cat2bug.jlog.vo.LogInfo;

import java.io.Closeable;
import java.util.Set;

/**
 * 日志服务
 */
public interface ILogService extends Closeable {
    /**
     * 设置缓存秒数
     * @param second 秒数
     */
    public void setCacheTime(long second);

    /**
     * 设置缓存数量
     * @param size 数量
     */
    public void setCacheSize(long size);

    /**
     * 推送日志
     * @param log 日志
     */
    public void putLog(ILoggingEvent log);
}
