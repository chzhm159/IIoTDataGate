package org.idw.core.bootconfig;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.idw.core.model.Device;
import org.idw.core.model.DeviceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class ChannelInitializerDispatch extends ChannelInitializer {
    private static final Logger log = LoggerFactory.getLogger(ChannelInitializerDispatch.class);
    private DeviceManager deviceManager;
    private HashMap<String, ProtocolAssembler> protocolHandlers = new HashMap();
    public ChannelInitializerDispatch(DeviceManager dm){
        this.deviceManager = dm;
        // TODO 这里未来计划通过注解的方式标注在对应的文件上,而不是这样写死,但目前先跑通功能为主
        protocolHandlers.put(Device.Protocols.upperlink.getName(),new UpperLinkAssembler());
        protocolHandlers.put(Device.Protocols.s7.getName(),new S7Assembler());
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        String longCId = ch.id().asLongText();
        log.debug("Channel初始化 {}",longCId);
        Device device = this.deviceManager.getDeviceByChannelId(ch.id());
        if(device != null){
            String protType = device.getProtocolType();
            ProtocolAssembler hc = protocolHandlers.get(protType);
            hc.initHandler(ch);
        }else {
            log.error("未能根据 ChannelId 匹配到对应的 Device 对象! {}",longCId);
        }
    }
}
