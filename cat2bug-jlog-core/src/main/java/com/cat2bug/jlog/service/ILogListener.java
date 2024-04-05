package com.cat2bug.jlog.service;

import com.cat2bug.jlog.vo.LogInfo;

import java.util.Set;

/**
 * 日志监听
 */
public interface ILogListener {
    /**
     * 间隔采集日志事件
     * @param logInfos 日志集合
     */
    void onInterval(Set<LogInfo> logInfos);

    /**
     * 缓存已满事件
     * @param logInfos 日志集合
     */
    void onCacheFull(Set<LogInfo> logInfos);

    /**
     * 日志服务关闭
     * @param logInfos 日志集合
     */
    void onClose(Set<LogInfo> logInfos);
}
