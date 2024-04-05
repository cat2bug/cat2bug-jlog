package com.cat2bug.jlog.service;

import com.cat2bug.jlog.vo.LogInfo;

import java.util.Set;

/**
 * 报告服务
 */
public interface IReportService {
    /**
     * 处理报告
     * @param logInfos 日志结合
     */
    public void handle(Set<LogInfo> logInfos);
}
