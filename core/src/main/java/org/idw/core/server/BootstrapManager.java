package org.idw.core.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.idw.core.model.Device;
import org.idw.core.model.DeviceManager;

import java.util.HashMap;

public class BootstrapManager {
    private static BootstrapManager instance = null;
    private BootstrapManager(){}
    public static BootstrapManager getInstance() {
        if(instance == null){
            //创建实例之前可能会有一些准备性的耗时工作
            // Thread.sleep(300);
            synchronized (DeviceManager.class) {
                if(instance == null){//二次检查
                    instance = new BootstrapManager();
                }
            }
        }
        return instance;
    }

    public void setUp(DeviceManager devManager){
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.TCP_NODELAY,true);
        b.option(ChannelOption.SO_LINGER,1);
        HashMap<String, Device> devList = devManager.getDevices();
        b.handler(new ChannelInitializerDispatch(devManager));

        devList.forEach((id,dev)->{
            String host = dev.getHost();
            int port = dev.getPort();
            try {
                ChannelFuture f = b.connect(host, port).sync();
                dev.setChannelFuture(f);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
