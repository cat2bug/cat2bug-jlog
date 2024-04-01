package com.cat2bug.jlog;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * @Author: yuzhantao
 * @CreateTime: 2024-04-01 22:13
 * @Version: 1.0.0
 */
public class Cat2BugAppender  extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        System.out.println("================Log event: " + iLoggingEvent.getFormattedMessage());
    }
}
