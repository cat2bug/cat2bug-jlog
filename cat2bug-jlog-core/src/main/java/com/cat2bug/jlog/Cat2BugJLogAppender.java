package com.cat2bug.jlog;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.cat2bug.common.util.AppUtil;
import com.cat2bug.common.util.ConsoleUtil;
import com.cat2bug.common.util.StringUtil;
import com.cat2bug.jlog.service.ILogListener;
import com.cat2bug.jlog.service.ILogService;
import com.cat2bug.jlog.service.IReportService;
import com.cat2bug.jlog.service.impl.LogServiceImpl;
import com.cat2bug.jlog.service.impl.PushReportServiceImpl;
import com.cat2bug.jlog.util.ReportUtil;
import com.cat2bug.jlog.vo.LogInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Cat2Bug日志采集服务
 */
public class Cat2BugJLogAppender extends AppenderBase<ILoggingEvent> implements ILogListener {
    private static final Logger log = LoggerFactory.getLogger(Cat2BugJLogAppender.class);
    /** 默认JLOG版本 */
    private final static String DEFAULT_APP_VERSION = "0.0.1";
    /** 日志服务 */
    private ILogService logService;

    private List<IReportService> reportServiceList;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 项目版本
     */
    private String projectVersion;
    /**
     * 提交到远程的主机地址
     */
    private String host;
    /**
     * 项目Key
     */
    private String projectKey;
    /**
     * 处理人
     */
    private String handler;
    /**
     * 是否显示Banner
     */
    private boolean banner = true;
    /**
     * 日志缓存最大数量,超出此将创建新报告，不超出每次提交只更新报告
     */
    private int logCacheSize=5;
    /**
     * 报告提交的时间周期
     */
    private long reportPushTime=5000;
    /**
     * 监控的包路径或类集合
     */
    private String monitorPackages;
    /**
     * 监控的异常类集合
     */
    private String monitorThrowable;
    /**
     * 忽略不处理的包路径或类集合
     */
    private String ignorePackages;
    /**
     * 忽略异常类集合
     */
    private String ignoreThrowable;
    /**
     * 报告key
     */
    private static String REPORT_KEY;

    @Override
    public void start() {
        super.start();
        // 初始化日志服务
        logService = new LogServiceImpl(this);
        logService.setCacheSize(logCacheSize);
        logService.setCacheTime(reportPushTime);
        this.initReportServices();
        if(this.banner) {
            // 绘制Logo
            ConsoleUtil.drawBanner("cat2bug-jlog-banner.txt", "cat2bug-jlog", this.getAppVersion());
            // 绘制彩色横条
            ConsoleUtil.drawColorLine();
        } else {
            log.info("Cat2Bug-JLog Started!");
        }
    }

    /**
     * 初始化报告服务集合
     */
    private void initReportServices() {
        Cat2BugJLogAppender.REPORT_KEY = ReportUtil.createReportKey();
        if(reportServiceList!=null) {
            reportServiceList.clear();
        } else {
            reportServiceList = new ArrayList<>();
        }
        reportServiceList.add(new PushReportServiceImpl(this.projectName,this.projectVersion,this.host,this.projectKey,this.handler,REPORT_KEY));
    }

    @Override
    public void stop() {
        super.stop();
        try {
            this.logService.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.logService = null;
    }

    @Override
    protected void append(ILoggingEvent loggingEvent) {
        if((StringUtil.isBlank(monitorPackages) ||
                Arrays.stream(monitorPackages.split(",")).anyMatch(p-> Arrays.stream(loggingEvent.getThrowableProxy().getStackTraceElementProxyArray()).anyMatch(l->l.getStackTraceElement().getClassName().indexOf(p)==0))) &&
           (StringUtil.isBlank(monitorThrowable) ||
                   Arrays.stream(monitorThrowable.split(",")).anyMatch(t-> loggingEvent.getThrowableProxy()!=null && loggingEvent.getThrowableProxy().getClassName().equals(t))==false) &&
            (StringUtil.isBlank(ignorePackages) ||
                    Arrays.stream(ignorePackages.split(",")).anyMatch(p->Arrays.stream(loggingEvent.getThrowableProxy().getStackTraceElementProxyArray()).anyMatch(l->l.getStackTraceElement().getClassName().indexOf(p)==0))) &&
            (StringUtil.isBlank(ignoreThrowable) ||
                    Arrays.stream(ignoreThrowable.split(",")).anyMatch(t-> loggingEvent.getThrowableProxy()!=null && loggingEvent.getThrowableProxy().getClassName().equals(t))==false)
            )
        this.logService.putLog(loggingEvent);
    }

    /**
     * 间隔时间已到
     * @param logInfos 日志集合
     */
    @Override
    public void onInterval(Set<LogInfo> logInfos) {
        reportServiceList.forEach(r->r.handle(logInfos));
    }

    /**
     * 日志缓存已满
     * @param logInfos 日志集合
     */
    @Override
    public void onCacheFull(Set<LogInfo> logInfos) {
        reportServiceList.forEach(r->r.handle(logInfos));

        this.initReportServices();  // 重新初始化报表服务
    }

    /**
     * 日志服务关闭
     * @param logInfos 日志集合
     */
    @Override
    public void onClose(Set<LogInfo> logInfos) {
        reportServiceList.forEach(r->r.handle(logInfos));
    }

    /**
     * 获取应用版本
     * @return 应用版本
     */
    private String getAppVersion() {
        String version = AppUtil.getValue("cat2bug.jlog.version");
        return StringUtil.isNotBlank(version)?version: DEFAULT_APP_VERSION;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public boolean isBanner() {
        return banner;
    }

    public void setBanner(boolean banner) {
        this.banner = banner;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getMonitorPackages() {
        return monitorPackages;
    }

    public void setMonitorPackages(String monitorPackages) {
        this.monitorPackages = monitorPackages;
    }

    public String getMonitorThrowable() {
        return monitorThrowable;
    }

    public void setMonitorThrowable(String monitorThrowable) {
        this.monitorThrowable = monitorThrowable;
    }

    public String getIgnorePackages() {
        return ignorePackages;
    }

    public void setIgnorePackages(String ignorePackages) {
        this.ignorePackages = ignorePackages;
    }

    public String getIgnoreThrowable() {
        return ignoreThrowable;
    }

    public void setIgnoreThrowable(String ignoreThrowable) {
        this.ignoreThrowable = ignoreThrowable;
    }

    public int getLogCacheSize() {
        return logCacheSize;
    }

    public void setLogCacheSize(int logCacheSize) {
        this.logCacheSize = logCacheSize;
    }

    public long getReportPushTime() {
        return reportPushTime;
    }

    public void setReportPushTime(long reportPushTime) {
        this.reportPushTime = reportPushTime;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }
}
