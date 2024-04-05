package com.cat2bug.jlog.util;

import java.util.UUID;

/**
 * 报告工具
 */
public class ReportUtil {
    /**
     * 创建报告KEY
     * @return KEY值
     */
    public static String createReportKey() {
        return UUID.randomUUID().toString();
    }
}
