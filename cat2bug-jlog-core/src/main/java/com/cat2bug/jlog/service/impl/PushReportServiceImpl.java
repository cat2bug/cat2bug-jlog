package com.cat2bug.jlog.service.impl;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.cat2bug.common.api.ReportService;
import com.cat2bug.common.util.AppUtil;
import com.cat2bug.common.util.StringUtil;
import com.cat2bug.common.vo.DefectState;
import com.cat2bug.common.vo.DefectType;
import com.cat2bug.common.vo.DefectVo;
import com.cat2bug.common.vo.ReportVo;
import com.cat2bug.jlog.exception.PushException;
import com.cat2bug.jlog.service.IReportService;
import com.cat2bug.jlog.vo.LogInfo;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 推送报告服务
 */
public class PushReportServiceImpl implements IReportService {
    private static final Logger log = LoggerFactory.getLogger(PushReportServiceImpl.class);
    private final static String APP_VERSION = "cat2bug.jlog.version";

    private final static String DEFECT_TITLE_HEADER = "日志异常:";

    private String projectName;

    private String projectVersion;

    private String host;

    private String projectKey;

    private String handler;

    private String reportKey;

    private static long pushCount;

    public PushReportServiceImpl(String projectName, String projectVersion, String host, String projectKey,String handler,String reportKey){
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.host = host;
        this.projectKey = projectKey;
        this.handler = handler;
        this.reportKey = reportKey;
    }

    /**
     * 推送处理报告
     * @param logInfos 日志结合
     */
    @Override
    public void handle(Set<LogInfo> logInfos) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ReportVo<List<DefectVo>> report = new ReportVo();
        report.setReportKey(this.reportKey);
        report.setReportTime(new Date());
        report.setReportTitle(Arrays.asList(
                "日志异常监控报告:",
                StringUtil.isBlank(this.projectName)?"":this.projectName,
                String.format("[thread:%d]", Thread.currentThread().getId())
        ).stream().collect(Collectors.joining("\n")));
        String description = Arrays.asList(
                this.getEnvironmentInfo("Report TIme", sdf.format(new Date())),
                String.format("* 提交次数:%d",++pushCount),
                this.getDataDetails(logInfos)).stream().collect(Collectors.joining("\n"));
        report.setReportDescription(description);
        List<DefectVo> defectVoList = getDefectList(logInfos);
        report.setReportData(defectVoList);
        if(StringUtil.isNotBlank(handler)) {
            report.setHandler(handler);
        }
        try {
            Response response = ReportService.pushDefect(this.host, this.projectKey, report);
            if(response.code()!= 200)
                throw new PushException(String.format("返回状态码异常, State Code:%s",response.code()));

            log.info("推送异常日志报告成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<DefectVo> getDefectList(Set<LogInfo> logInfos) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return logInfos.stream().map(l->{
            DefectVo defectVo = new DefectVo();
            defectVo.setModuleVersion(projectVersion);
            defectVo.setDefectName(DEFECT_TITLE_HEADER+l.getLoggingEvent().getLoggerName());
            StringBuffer describe = new StringBuffer();
            describe.append(getEnvironmentInfo("Log Time",sdf.format(new Date(l.getUpdateTime())))+"\n");
//            describe.append("* Class:"+l.getLoggingEvent().getMessage()+"\n");
            describe.append("````java\n");
            describe.append(l.getLoggingEvent().getThrowableProxy().getClassName()+":");
            describe.append(l.getLoggingEvent().getThrowableProxy().getMessage()+"\n");
            for(StackTraceElementProxy t : l.getLoggingEvent().getThrowableProxy().getStackTraceElementProxyArray()) {
                describe.append(t.toString()+"\n");
            }
            describe.append("````");
            defectVo.setDefectDescribe(describe.toString());
            defectVo.setDefectState(DefectState.PROCESSING);
            defectVo.setDefectType(DefectType.BUG);
            defectVo.setDefectGroupKey(getGroupKey(l));
            defectVo.setDefectKey(getKey(l));
            return defectVo;
        }).collect(Collectors.toList());
    }

    private String getGroupKey(LogInfo log) {
        return log.getLoggingEvent().getThrowableProxy().getClassName();
    }

    private String getKey(LogInfo log) {
        return log.getLoggingEvent().getThrowableProxy().getClassName()+"."+log.getLoggingEvent().getThrowableProxy().getMessage();
    }

    /**
     * 获取环境信息
     * @param timeTitleName 时间标题名
     * @param timeValue 时间
     * @return 环境信息
     */
    private String getEnvironmentInfo(String timeTitleName, String timeValue) {
        String osName = System.getProperty("os.name");// 操作系统名称
        String osArch = System.getProperty("os.arch");// 获取操作系统架构（位数）
        String osVersion = System.getProperty("os.version");// 获取操作系统版本
        String javaVersion = System.getProperty("java.version"); // java版本
        List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments(); // jvm参数
        return Arrays.asList(
                "## 基础信息",
                "* **"+timeTitleName+":** "+timeValue,
                "* **Tools:** Cat2Bug-JLog",
                "* **Tools Version:** "+ AppUtil.getValue(APP_VERSION),
                "* **OS Name:** "+osName,
                "* **OS Version:** "+osVersion,
                "* **OS Arch:** "+osArch,
                "* **Java Version:** "+javaVersion,
                "* **JVM Args:** "+jvmArgs.stream().collect(Collectors.joining(";"))
        ).stream().collect(Collectors.joining("\n"));
    }

    /**
     * 获取数据详情
     * @param logInfos 日志集合
     * @return 数据详情
     */
    private String getDataDetails(Set<LogInfo> logInfos) {
        StringBuffer ret = new StringBuffer();
        ret.append("## 数据报告详情\n");

        // 设置表格标题
        List<String> titles = Arrays.asList(
                "类",
                "方法",
                "行数",
                "次数",
                "更新时间",
                "简述"
        );
        ret.append(" | "+titles.stream().collect(Collectors.joining(" | "))+" | \n");
        ret.append(" | "+titles.stream().map(m->"---------").collect(Collectors.joining(" | "))+" | \n");

        // 设置表格数据行
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
        ret.append(logInfos.stream().map(log->{
            StackTraceElementProxy[] traces = log.getLoggingEvent().getThrowableProxy().getStackTraceElementProxyArray();
            StackTraceElement trace = traces!=null && traces.length>0?traces[0].getStackTraceElement():null;
            return " | "+Arrays.asList(
                    trace!=null?trace.getClassName():"",
                    trace!=null?trace.getMethodName():"",
                    trace!=null?trace.getLineNumber()+"":"",
                    log.getCount()+"",
                    dateFormat.format(log.getUpdateTime()),
                    log.getLoggingEvent().getThrowableProxy().getClassName() +":"+ log.getLoggingEvent().getMessage()
            ).stream().collect(Collectors.joining(" | "))+" | ";
        }).collect(Collectors.joining("\n")));

        return ret.toString();
    }
}
