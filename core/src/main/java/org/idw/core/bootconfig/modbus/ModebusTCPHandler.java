package org.idw.core.bootconfig.modbus;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import org.apache.commons.lang3.StringUtils;
import org.idw.core.bootconfig.FakeReadJob;
import org.idw.core.bootconfig.WriteHandler;
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

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class ModebusTCPHandler extends ChannelDuplexHandler implements JobListener, WriteHandler {
    private static final Logger log = LoggerFactory.getLogger(ModebusTCPHandler.class);
    private Device device;
    private ModbusMasterTCP modbusMasterTCP;
    private volatile long readTimeout = 3000;
    private volatile long writeTimeout = 3000;
    TransactionId txid = TransactionId.getInst(0,65535,3000);
    // transactionId与Tag.key的对应关系
    private HashMap<String,String> tid2tkey= new HashMap<String,String>();

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
        HashMap<String,Object> opt = new HashMap<String,Object>();
        opt.put("registerType",tag.getRegisterType());
        opt.put("registerIndex",tag.getRegisterIndex());
        opt.put("unit",tag.getUnit());
        opt.put("opt","write");
        opt.put("count", data.getCount());
        opt.put("data",data.getData());
        int tid = txid.getSID(tag.getTimeout());
        opt.put("transactionId",tid);
        tid2tkey.put(String.valueOf(tid),tag.getKey());
        opt.put("unitId",tag.getDevice().getDeviceCode());
        ByteBuf cmd = modbusMasterTCP.encode(opt);
        if(cmd==null){
            log.error("变量[{}]无法写入 ",tag.getKey());
            return ;
        }
        send(tag.getKey(),cmd);
    }


    private void doReadJob(JobExecutionContext context){
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        String tagKey = jobData.getString("tagKey");
        Tag  tag = this.device.getTag(tagKey);
        if(tag==null){
            return ;
        }
        readTimeout = tag.getTimeout();
        ByteBuf readCmd = tag.getReadCmd();
        int tid = txid.getSID(tag.getTimeout());
        tid2tkey.put(String.valueOf(tid),tag.getKey());
        readCmd.setShort(0,tid);
        send(tag.getKey(),readCmd);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf content = (ByteBuf) msg;
        int tid = -1;
        try{
            // log.debug("receive data:\r\n {}", ByteBufUtil.prettyHexDump(content));
            // ModbusTcpPayload payload = (ModbusTcpPayload)modbusMasterTCP.decode(content);
            int sid = content.readUnsignedShort();
//            if(sid>-1){
//                log.error("收到数据,但未能正常解包,可能是网络异常");
//                return ;
//            }
//            tid = payload.getTransactionId();
            String txid = String.valueOf(sid);
            if(tid2tkey.containsKey(txid)){
                String tagKey = tid2tkey.get(txid);
                Tag tag = device.getTag(tagKey);
                if(tag!=null){
                    content.resetReaderIndex();
                    tag.onValue(content);
                }
            }else{
                log.error("设备[{}],收到数据,但未找到会话[{}]对应的 Tag 对象 " ,device.getDeviceID(),tid);
            }
        }catch (Exception e){
            log.error("设备[{}],收到数据,但处理异常:{} " ,device.getDeviceID(),e.getStackTrace());
        }finally {
            txid.releaseSID(tid);
        }
    }

    private void send(String tagKey,ByteBuf data){
        try {
            log.debug("[{}] send data:\r\n {}",tagKey,ByteBufUtil.prettyHexDump(data));
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

    private void genReadCmd(Tag tag){
        HashMap<String,Object> opt = new HashMap<String,Object>();
        opt.put("registerType",tag.getRegisterType());
        opt.put("registerIndex",tag.getRegisterIndex());
        opt.put("unit",tag.getUnit());
        opt.put("opt","read");
        opt.put("count",tag.getCount());
        opt.put("transactionId",0);
        opt.put("unitId",tag.getDevice().getDeviceCode());
        try{
            ByteBuf cmd = modbusMasterTCP.encode(opt);
            tag.setReadCmd(cmd);
        }catch (Exception e){
            log.error(">>>");
            log.error("[{}]构造读取命令异常:{},{}",tag.getKey(),e.getCause(),e.getStackTrace());
            log.error("<<<");
        }

        /*Byte[] list2 = new Byte[cmd.size()];
        byte[] cmdbyte = ArrayUtils.toPrimitive(cmd.toArray(list2));
        ByteBuf cmdByteBuf = Unpooled.wrappedBuffer(cmdbyte);*/

    }



    @Override
    public String getName() {
        return this.device.getDeviceID();
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        doReadJob(context);
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {}

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {}
}