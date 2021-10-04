package org.idw.core.bootconfig;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.idw.core.model.Device;
import org.idw.core.model.Tag;
import org.idw.core.model.TagData4Write;
import org.idw.protocol.keyence.UpperLink;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * 本类三大块功能
 * 1. 首先是作为 ChannelHandler 接收 Channel 的各种事件
 * 2. 其次需要根据 Device 中的 Tag 列表启动定时器
 * 3. 收到数据后将数据映射到 Tag 中
 * 4. 链接状态的维护,断线重连等等
 * 5. 如何处理 write 操作?
 */
public class UpperlinkHandler extends ChannelDuplexHandler implements JobListener, OnTagWriteListener{

    private static final Logger log = LoggerFactory.getLogger(UpperlinkHandler.class);

    private UpperLink upperLinkProtocol;
    private Device device;
    private Exchanger<Object> exchanger = new Exchanger<>();
    private AtomicBoolean sended = new AtomicBoolean(false);
    private volatile int readTimeout = 3000;
    private volatile int writeTimeout = 3000;
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
    public void doWrite(Tag tag, TagData4Write data){
        HashMap<String,Object> opt = new HashMap<String,Object>();
        opt.put("registerType",tag.getRegisterType());
        opt.put("registerIndex",tag.getRegisterIndex());
        opt.put("unit",tag.getUnit());
        int count = data.getCount();
        opt.put("count",count);
        opt.put("opt","write");
        opt.put("data",data.getData().toString());
        ByteBuf cmd = upperLinkProtocol.encode(opt);
        if(cmd==null){
            log.error("变量[{}]写入失败 ",tag.getKey());
            return ;
        }
        /*Byte[] list2 = new Byte[cmd.size()];
        byte[] cmdbyte = ArrayUtils.toPrimitive(cmd.toArray(list2));
        ByteBuf cmdByteBuf = Unpooled.wrappedBuffer(cmdbyte);*/
        ByteBuf recData = sendSync(tag.getKey(),cmd);
        if(recData!=null){
            if(tag.getDataStrategy().equalsIgnoreCase("raw")){
                tag.onValue(recData);
            }
        }else{
            log.error("写入操作,返回空数据");
        }
    }

    private void doReadJob(JobExecutionContext context){
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        String tagKey = jobData.getString("tagKey");
        String devID = this.device.getDeviceID();
        Tag  tag = this.device.getTag(tagKey);
        if(tag==null){
            return ;
        }
        readTimeout = tag.getCmdTimeout();
        ByteBuf readCmd = tag.getReadCmd();
        ByteBuf recData = sendSync(tag.getKey(),readCmd);
        if(recData!=null){
            if(tag.getDataStrategy().equalsIgnoreCase("raw")){
                tag.onValue(recData);
            }
        }else{
            log.error("读取操作,返回空数据");
        }
    }
    private ByteBuf sendSync(String tagKey,ByteBuf data){
        // 不可并发
        synchronized (this){
            ChannelFuture channelFuture = this.device.getChannelFuture();
            Channel channel = channelFuture.channel();
            // 保证 先发送后接受 ---1
            boolean ret = sended.compareAndSet(false,true);
            if(!ret){
                return null;
            }
            // 发送数据
            channel.writeAndFlush(data);
            try {
                log.debug("设备[{}]中变量[{}] 已发送指令,等待结果中...1...",this.device.getDeviceID(),tagKey);
                ByteBuf recData = (ByteBuf)exchanger.exchange(tagKey,readTimeout,TimeUnit.MILLISECONDS);
                log.debug("设备[{}]中变量[{}] 收到返回结果,并交由上层处理...2...",this.device.getDeviceID(),tagKey);
                return recData;
            }catch (Exception e){
                e.printStackTrace();
                log.error("设备[{}]中变量[{}]发送指令后等待结果超时{}",this.device.getDeviceID(),e.getStackTrace());
                return null;
            }
        }
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf content = (ByteBuf) msg;
        // 保证 先发送后接受 ---2
        boolean ret = sended.compareAndSet(true,false);
        if(!ret){
            log.warn("收到数据,但被丢弃,因可能是网络原因重复数据或者设备主动发送的数据");
            return ;
        }
        try{
            String tagKey = (String)exchanger.exchange(content,readTimeout,TimeUnit.MILLISECONDS);
            log.debug("设备=[{}],变量=[{}] 数据处理完毕...3..." ,device.getDeviceID(),tagKey);
        }catch (Exception e){
            log.error("设备[{}],收到数据,但因发送指令超时导致异常:{} " ,device.getDeviceID(),e.getStackTrace());
        }
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
            if(StringUtils.equalsIgnoreCase(tag.getOperate(),"rw")||StringUtils.equalsIgnoreCase(tag.getOperate(),"r")){
                genReadCmd(tag);
            }
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
                                .withIntervalInMilliseconds(tag.getReadInterval())
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
    private void genReadCmd(Tag tag){
        HashMap<String,Object> opt = new HashMap<String,Object>();
        opt.put("registerType",tag.getRegisterType());
        opt.put("registerIndex",tag.getRegisterIndex());
        opt.put("unit","uint16");
        opt.put("opt","read");
        opt.put("count",tag.getCount());
        ByteBuf cmd = upperLinkProtocol.encode(opt);
        /*Byte[] list2 = new Byte[cmd.size()];
        byte[] cmdbyte = ArrayUtils.toPrimitive(cmd.toArray(list2));
        ByteBuf cmdByteBuf = Unpooled.wrappedBuffer(cmdbyte);*/
        tag.setReadCmd(cmd);
    }

    @Override
    public String getName() {
        return this.device.getDeviceID();
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        doReadJob(context);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {

    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {

    }


}
