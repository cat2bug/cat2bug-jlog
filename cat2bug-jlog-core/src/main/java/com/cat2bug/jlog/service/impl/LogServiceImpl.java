package com.cat2bug.jlog.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.cat2bug.jlog.service.ILogListener;
import com.cat2bug.jlog.service.ILogService;
import com.cat2bug.jlog.vo.LogInfo;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 日志服务
 */
public class LogServiceImpl implements ILogService {
    private Map<String, LogInfo> logMap = new ConcurrentHashMap<>();

    private ILogListener logListener;

    /**
     * 缓存
     */
    private long cacheSize;
    /**
     * 缓存的有效时间
     */
    private long cacheTime;
    /**
     * 上一次刷新时间
     */
    private long prevRefreshTime = System.currentTimeMillis();

    public LogServiceImpl(ILogListener logListener) {
        this.logListener = logListener;
    }

    @Override
    public void setCacheTime(long time) {
        this.cacheTime=time;
    }

    @Override
    public void setCacheSize(long size) {
        this.cacheSize=size;
    }

    @Override
    public void putLog(ILoggingEvent log) {
        // 如果不是异常错误，不处理
        if(log.getLevel() != Level.ERROR)
            return;

        String key = getKey(log);
        if (logMap.containsKey(key)==false) {
            logMap.put(key, new LogInfo());
        }

        LogInfo logInfo = logMap.get(key);
        logInfo.setCount(1 + logInfo.getCount());
        logInfo.setUpdateTime(System.currentTimeMillis());
        logInfo.setLoggingEvent(log);
        if(logMap.size()>=this.cacheSize && this.cacheSize>0) {
            this.onCacheFull();
        } else if((System.currentTimeMillis()-prevRefreshTime)>cacheTime) {
            onCacheTimeUp();
        }
    }

    /**
     * 刷新缓存
     */
    private void onCacheFull() {
        Set<LogInfo> logs = this.getLogs();
        this.logMap.clear();
        if(this.logListener != null) {
            this.logListener.onCacheFull(logs);
        }
    }

    /**
     * 缓存时间超过预定时间事件
     */
    private void onCacheTimeUp() {
        this.prevRefreshTime = System.currentTimeMillis();
        if(this.logListener != null) {
            this.logListener.onInterval(this.getLogs());
        }
    }

    @Override
    public void close() throws IOException {
        this.prevRefreshTime = System.currentTimeMillis();
        Set<LogInfo> logs = this.getLogs();
        this.logMap.clear();
        if(this.logListener != null) {
            this.logListener.onCacheFull(logs);
        }
        this.logListener = null;
    }

    /**
     * 获取日志集合
     * @return 日志结合
     */
    private Set<LogInfo> getLogs() {
        return logMap.values().stream().collect(Collectors.toSet());
    }

    /**
     * 获取日志唯一标识符
     * @param log 日志
     * @return 唯一标识符
     */
    private String getKey(ILoggingEvent log) {
        if(log.getCallerData()!=null&&log.getCallerData().length>0) {
            StackTraceElement ste = log.getCallerData()[0];
            return ste.getLineNumber()+ste.getClassName()+ste.getMethodName()+log.getThrowableProxy().getMessage();
        }
        return log.getThrowableProxy().getMessage();
    }

}
