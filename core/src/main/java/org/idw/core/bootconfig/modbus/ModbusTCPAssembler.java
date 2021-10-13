package org.idw.core.bootconfig.modbus;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.idw.core.bootconfig.ProtocolAssembler;
import org.idw.core.model.Device;
import org.idw.core.model.DeviceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModbusTCPAssembler extends ProtocolAssembler {
    private static final Logger log = LoggerFactory.getLogger(ModbusTCPAssembler.class);
    @Override
    public void initHandler(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        ChannelId cid = ch.id();
        DeviceManager dm = DeviceManager.getInstance();
        Device dev = dm.getDeviceByChannelId(cid);
        // TODO LengthFieldBasedFrameDecoder的4个参数有待验证
        LengthFieldBasedFrameDecoder ld = new LengthFieldBasedFrameDecoder(4096,4,2,0,0);
        ModebusTCPHandler dh = new ModebusTCPHandler(dev);
        pipeline.addLast("LengthFieldBasedFrameDecoder",ld);
        pipeline.addLast("logicHandler",dh);

        // TODO 这里未来会重构,但是没想好如何处理... 主要是为了写入操作
        dev.setChannelHandler(dh);
        log.info("Modbus/TCP handler 配置完成");
    }
}
