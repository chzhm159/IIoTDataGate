package org.idw.core.bootconfig;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.idw.core.model.DeviceManager;
import org.idw.core.testanddemo.UpperLinkHandler;

import java.net.InetSocketAddress;

public class UpperLinkBootstrap extends ChannelInitializer {
    private DeviceManager devManager;

    public UpperLinkBootstrap(DeviceManager dm){
        this.devManager = dm;
    }

    public void initChannel(Channel channel) {
        InetSocketAddress remoteAddr = (InetSocketAddress)channel.remoteAddress();
        String hostString = remoteAddr.getHostString();
        int port  = remoteAddr.getPort();
        this.devManager.getDeviceByHostPort(hostString,port);
        channel.pipeline().addFirst(new LineBasedFrameDecoder(2048,true,false));
        channel.pipeline().addLast(new UpperLinkHandler());
    }
}
