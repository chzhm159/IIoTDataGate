package org.idw.core.bootconfig;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.idw.core.model.Device;
import org.idw.core.model.DeviceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public class DefaultLastHandler extends ChannelDuplexHandler {
    private static final Logger log = LoggerFactory.getLogger(DefaultLastHandler.class);
    private Device device;
    public DefaultLastHandler(Device dev){
        this.device = dev;
    }
    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
                        SocketAddress localAddress, ChannelPromise promise) throws Exception {
        super.connect(ctx, remoteAddress, localAddress,promise);
        log.debug("{}已建立链接",remoteAddress.toString());
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //在这里可以处理硬件发送过来的数据
        ByteBuf content = (ByteBuf) msg;
        log.debug("收到数据：{}" , ByteBufUtil.hexDump(content));
    }

}
