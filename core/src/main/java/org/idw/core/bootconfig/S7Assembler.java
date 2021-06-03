package org.idw.core.bootconfig;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S7Assembler extends ProtocolAssembler {
    private static final Logger log = LoggerFactory.getLogger(S7Assembler.class);
    public S7Assembler(){
    }

    @Override
    public void initHandler(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
//        pipeline.addFirst("linebaseDecoder",linebaseDecoder);
//        pipeline.addFirst("logicHandler",defaultLastHandler);
        log.info("s7协议 handler 配置完成");
    }
}
