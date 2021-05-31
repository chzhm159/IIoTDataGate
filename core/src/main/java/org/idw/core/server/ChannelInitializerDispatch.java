package org.idw.core.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.idw.core.model.Device;
import org.idw.core.model.DeviceManager;

import java.net.InetSocketAddress;

public class ChannelInitializerDispatch extends ChannelInitializer {
    private DeviceManager deviceManager;

    public ChannelInitializerDispatch(DeviceManager dm){
        this.deviceManager = dm;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        // TODO通过 ip,端口号找到对应的 device,然后添加对应的 handler
        InetSocketAddress remoteAddr = (InetSocketAddress)ch.remoteAddress();
        String hostString = remoteAddr.getHostString();
        int port  = remoteAddr.getPort();
        Device device = this.deviceManager.getDeviceByHostPort(hostString,port);
        if(device==null){}
        // device.channelRegistered();
    }
}
