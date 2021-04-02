package org.idw.core.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.idw.core.testanddemo.UpperLinkHandler;
import org.idw.core.utils.AcquireTagsDefine;
import org.idw.protocol.DataTypeNames;
import org.idw.core.utils.TagsDefineFileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    public static void main(String[] args) {
        AcquireTagsDefine atd = start();
        InitDevicesList(atd);
    }
    public static AcquireTagsDefine start(){
        TagsDefineFileProcessor tdfp = new TagsDefineFileProcessor();
        AcquireTagsDefine atd = tdfp.load("config/tags.json");
        log.info("加载plc协议 {}", DataTypeNames.int16);
        return atd;
    }
    public static void InitDevicesList(AcquireTagsDefine tagDefs){
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.TCP_NODELAY,true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addFirst(new LineBasedFrameDecoder(2048,true,false));
                    ch.pipeline().addLast(new UpperLinkHandler());
                }
            });
            // Start the client.
            ChannelFuture f = b.connect("192.168.0.40", 8501).sync(); // (5)
            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // workerGroup.shutdownGracefully();
        }
    }
}
