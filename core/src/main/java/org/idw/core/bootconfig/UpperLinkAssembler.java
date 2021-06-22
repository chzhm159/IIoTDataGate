package org.idw.core.bootconfig;

import io.netty.channel.*;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.idw.core.model.Device;
import org.idw.core.model.DeviceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UpperLinkAssembler extends ProtocolAssembler {
    private static final Logger log = LoggerFactory.getLogger(UpperLinkAssembler.class);

    @Override
    public void initHandler(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        ChannelId cid = ch.id();
        DeviceManager dm = DeviceManager.getInstance();
        Device dev = dm.getDeviceByChannelId(cid);
        LineBasedFrameDecoder ld = new LineBasedFrameDecoder(4096,true,true);
        UpperlinkHandler dh = new UpperlinkHandler(dev);
        // TODO 这里未来会重构,但是没想好如何处理...
        dev.setChannelHandler(dh);
        pipeline.addLast("linebaseDecoder",ld);
        pipeline.addLast("logicHandler",dh);
        log.info("上位链路协议 handler 配置完成");
    }
}
