package org.idw.core.bootconfig;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

public class UpperLinkHandler extends ChannelDuplexHandler {
    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
                        SocketAddress localAddress, ChannelPromise promise) throws Exception {
       super.connect(ctx,remoteAddress,localAddress,promise);

    }
}
