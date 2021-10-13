package org.idw.core.bootconfig.modbus;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import org.apache.commons.lang3.StringUtils;
import org.idw.core.bootconfig.FakeReadJob;
import org.idw.core.bootconfig.OnTagWriteListener;
import org.idw.core.bootconfig.TimeScheduler;
import org.idw.core.model.Device;
import org.idw.core.model.Tag;
import org.idw.core.model.TagData4Write;
import org.idw.protocol.modbus.ModbusMasterTCP;
import org.idw.protocol.modbus.ModbusTcpPayload;
import org.idw.protocol.modbus.TransactionId;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class ModebusTCPHandler extends ChannelDuplexHandler implements JobListener, OnTagWriteListener {
    private static final Logger log = LoggerFactory.getLogger(ModebusTCPHandler.class);
    private Device device;
    private ModbusMasterTCP modbusMasterTCP;
    private volatile int readTimeout = 3000;
    private volatile int writeTimeout = 3000;
    public ModebusTCPHandler(Device dev){
        this.device = dev;
        modbusMasterTCP= new ModbusMasterTCP();
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
                        SocketAddress localAddress, ChannelPromise promise) throws Exception {
        super.connect(ctx, remoteAddress, localAddress,promise);
        log.debug("{}已建立链接",remoteAddress.toString());
        start();
    }
    @Override
    public void doWrite(Tag tag, TagData4Write data) {

    }

    @Override
    public String getName() {
        return this.device.getDeviceID();
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {

    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {

    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        doReadJob(context);
    }

    private void doReadJob(JobExecutionContext context){
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        String tagKey = jobData.getString("tagKey");
        Tag  tag = this.device.getTag(tagKey);
        if(tag==null){
            return ;
        }
        readTimeout = tag.getCmdTimeout();
        ByteBuf readCmd = tag.getReadCmd();
        send(tag.getKey(),readCmd);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf content = (ByteBuf) msg;
        // log.debug("receive data:\n {}", ByteBufUtil.prettyHexDump(content));
        ModbusTcpPayload payload = (ModbusTcpPayload)modbusMasterTCP.decode(content);
        if(payload==null){
            log.error("收到数据,但未能正常解包,可能是网络异常");
            return ;
        }
        try{
            int tid = payload.getTransactionId();
            String txid = String.valueOf(tid);
            if(tid2tkey.containsKey(txid)){
                String tagKey = tid2tkey.get(txid);
                Tag tag = device.getTag(tagKey);
                if(tag!=null){
                    if(tag.getDataStrategy().equalsIgnoreCase("raw")){
                        content.resetReaderIndex();
                        tag.onValue(content);
                    }else{
                        tag.onValue(payload);
                    }
                }
            }else{
                log.error("设备[{}],收到数据,但未找到会话[{}]对应的 Tag 对象 " ,device.getDeviceID(),tid);
            }
        }catch (Exception e){
            log.error("设备[{}],收到数据,但处理异常:{} " ,device.getDeviceID(),e.getStackTrace());
        }
    }

    private void send(String tagKey,ByteBuf data){
        try {
            ChannelFuture channelFuture = this.device.getChannelFuture();
            Channel channel = channelFuture.channel();
            // 发送数据
            ChannelFuture sendFuture = channel.writeAndFlush(data);
        }catch (Exception e){
            e.printStackTrace();
            log.error("设备[{}]中变量[{}]发送异常:{}",this.device.getDeviceID(),e.getStackTrace());
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
    private HashMap<String,String> tid2tkey= new HashMap<String,String>();
    private void genReadCmd(Tag tag){
        TransactionId txid = TransactionId.getInst();
        HashMap<String,Object> opt = new HashMap<String,Object>();
        opt.put("registerType",tag.getRegisterType());
        opt.put("registerIndex",tag.getRegisterIndex());
        opt.put("unit",tag.getUnit());
        opt.put("opt","read");
        opt.put("count",tag.getCount());
        int tid = txid.getTransactionId();
        opt.put("transactionId",tid);
        tid2tkey.put(String.valueOf(tid),tag.getKey());
        opt.put("unitId",tag.getDevice().getDeviceCode());
        ByteBuf cmd = modbusMasterTCP.encode(opt);
        /*Byte[] list2 = new Byte[cmd.size()];
        byte[] cmdbyte = ArrayUtils.toPrimitive(cmd.toArray(list2));
        ByteBuf cmdByteBuf = Unpooled.wrappedBuffer(cmdbyte);*/
        tag.setReadCmd(cmd);
    }
}
