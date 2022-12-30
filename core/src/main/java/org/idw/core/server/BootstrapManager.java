package org.idw.core.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.idw.core.bootconfig.ChannelInitializerDispatch;
import org.idw.core.model.Device;
import org.idw.core.model.DeviceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class BootstrapManager {
    private static final Logger log = LoggerFactory.getLogger(BootstrapManager.class);
    private static BootstrapManager instance = null;
    private BootstrapManager(){}

    /**
     * 启动管理器实例.单例模式
     * @return
     */
    public static BootstrapManager getInstance() {
        if(instance == null){
            synchronized (DeviceManager.class) {
                if(instance == null){
                    instance = new BootstrapManager();
                }
            }
        }
        return instance;
    }

    /**
     * 根据设备列表建立对应的链接,绑定到特定的处理对象上
     * @param devManager
     */
    public void setUp(DeviceManager devManager){
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.TCP_NODELAY,true);
        b.option(ChannelOption.SO_LINGER,1);
        HashMap<String, Device> devList = devManager.getDevices();
        // 通过此处实现不同的协议 handler 的装配
        b.handler(new ChannelInitializerDispatch(devManager));

        devList.forEach((id,dev)->{
            String host = dev.getHost();
            int port = dev.getPort();
            ChannelFuture f = b.connect(host, port);
            dev.setChannelFuture(f);
            ChannelId cid = f.channel().id();
            log.info("绑定的ID为 {}",cid.asLongText());
            // TODO 处理链接失败的重试
        });
    }
}
