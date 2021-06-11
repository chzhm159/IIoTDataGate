package org.idw.core.bootconfig;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 主要是借助 Quartz 针对每个 tag 做定时读取
 */
@DisallowConcurrentExecution
public class FakeReadJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(FakeReadJob.class);
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
    }
}
