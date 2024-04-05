package com.cat2bug.jlog.vo;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * 日志信息
 */
public class LogInfo {
    /**
     * 日志事件
     */
    private ILoggingEvent loggingEvent;
    /**
     * 出现次数
     */
    private long count;
    /**
     * 更新时间
     */
    private long updateTime;

    public ILoggingEvent getLoggingEvent() {
        return loggingEvent;
    }

    public void setLoggingEvent(ILoggingEvent loggingEvent) {
        this.loggingEvent = loggingEvent;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
