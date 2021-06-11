package org.idw.core.bootconfig;

import com.google.common.collect.Queues;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.apache.commons.lang3.ArrayUtils;
import org.idw.core.model.Device;
import org.idw.core.model.Tag;
import org.idw.protocol.keyence.UpperLink;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * 本类三大块功能
 * 1. 首先是作为 ChannelHandler 接收 Channel 的各种事件
 * 2. 其次需要根据 Device 中的 Tag 列表启动定时器
 * 3. 收到数据后将数据映射到 Tag 中
 * 4. 链接状态的维护,断线重连等等
 */
public class UpperlinkHandler extends ChannelDuplexHandler implements JobListener{

    private static final Logger log = LoggerFactory.getLogger(UpperlinkHandler.class);

    private UpperLink upperLinkProtocol;
    private Device device;

    // 由于 上位链路协议 发送指令与返回数据包没有对应关系,所以只能模拟同步请求
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    public UpperlinkHandler(Device dev){
        this.device = dev;
        upperLinkProtocol= new UpperLink(dev.getDeviceModel());
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
                        SocketAddress localAddress, ChannelPromise promise) throws Exception {
        super.connect(ctx, remoteAddress, localAddress,promise);
        log.debug("{}已建立链接",remoteAddress.toString());
        start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //在这里可以处理硬件发送过来的数据

        // String tagKey  = request.take();
        ByteBuf content = (ByteBuf) msg;
        log.debug("设备[{}],变量[{}] 收到数据：{}" ,device.getDeviceID(),tagKey, ByteBufUtil.hexDump(content));
        request.release();
        // countDownLatch.countDown();
        // this.notify();


    }

    /**
     * 每个 Device 初始化一个定时器, 并将每个 Tag 配置的采集周期映射为一个 Job
     */
    private void start(){
        TimeScheduler timeScheduler = TimeScheduler.getInstance();
        Scheduler scheduler = timeScheduler.getScheduler(this.device);
        ConcurrentHashMap<String, Tag> tagList = this.device.getTags();
        String jobGroup = this.device.getDeviceID();
        tagList.forEach((tagKey,tag)->{
            try {
                JobDataMap jdm = new JobDataMap();
                JobDetail job = newJob(FakeReadJob.class)
                        .withIdentity(tag.getKey(), jobGroup)
                        .usingJobData("tagKey",tag.getKey())
                        .usingJobData(jdm)
                        .build();
                // Trigger the job to run now, and then repeat every 40 seconds
                Trigger trigger = newTrigger()
                        .withIdentity(tag.getKey(), jobGroup)
                        .startNow()
                        .withSchedule(simpleSchedule()
                                .withIntervalInSeconds(5)
                                .repeatForever())
                        .build();

                // Tell quartz to schedule the job using our trigger
                scheduler.scheduleJob(job, trigger);
            } catch (SchedulerException e) {
                e.printStackTrace();
                log.error("设备[{}],定时器配置异常:{}",this.device.getDeviceID(),e.getStackTrace());
            }
        });
        try {
            scheduler.getListenerManager().addJobListener(this);
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
            log.error("设备[{}],定时器启动异常:{}",this.device.getDeviceID(),e.getStackTrace());
        }
    }

    private Semaphore  request = new Semaphore (0);
    private void sendSync(JobExecutionContext context){
        synchronized (this){
            JobDataMap jobData = context.getJobDetail().getJobDataMap();
            String tagKey = jobData.getString("tagKey");
            String devID = this.device.getDeviceID();
            Tag  tag = this.device.getTag(tagKey);
            if(tag==null){
                return ;
            }
            ChannelFuture channelFuture = this.device.getChannelFuture();
            Channel channel = channelFuture.channel();
            HashMap<String,Object> opt = new HashMap<String,Object>();
            opt.put("registerType",tag.getRegisterType());
            opt.put("registerIndex",tag.getRegisterIndex());
            opt.put("unit","uint16");
            opt.put("count",tag.getCount());
            ArrayList<Byte> cmd = upperLinkProtocol.getReadCommand(opt);
            Byte[] list2 = new Byte[cmd.size()];
            byte[] cmdbyte = ArrayUtils.toPrimitive(cmd.toArray(list2));
            ByteBuf out = Unpooled.wrappedBuffer(cmdbyte);
            channel.writeAndFlush(out);
            try {
                request.acquire(1);
                // request.await(60000,TimeUnit.SECONDS);
                log.debug("设备[{}]变量[{}]已发送指令",devID,tag.getKey());
            }catch (Exception e){
                log.error("设备[{}]中变量[{}]指令进入队列失败: {}",devID,tag.getKey(),e.getStackTrace());
                e.printStackTrace();
                return ;
            }
            try {

                log.debug("================ countDownLatch.count={} ",countDownLatch.getCount());
                // this.wait(1000000);
                // boolean timeout = countDownLatch.await(60,TimeUnit.SECONDS);
                log.debug("================ timeout={},countDownLatch.count={} ","",countDownLatch.getCount());
//                if(!timeout){
//                    String waitTagKey= request.take();
//                    log.debug("设备[{}]变量[{}]等待结果超时,移除队列",devID,waitTagKey);
//                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("设备[{}]中变量[{}]发送指令后等待结果超时{}",devID,tag.getKey(),e.getStackTrace());
            }
            log.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    @Override
    public String getName() {
        return this.device.getDeviceID();
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        sendSync(context);

    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {

    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {

    }
}
