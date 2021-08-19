package org.idw.core.bootconfig;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 主要是借助 Quartz 针对每个 tag 做定时读取,
 * 上位链路协议,目前没有想到好的并发处理方式,所以只能模拟同步请求,导致这里的job执行不允许并发,所以设置了 DisallowConcurrentExecution
 */
@DisallowConcurrentExecution
public class FakeReadJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(FakeReadJob.class);
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

    }
}
