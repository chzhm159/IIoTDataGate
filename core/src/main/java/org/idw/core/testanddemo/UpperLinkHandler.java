package org.idw.core.testanddemo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.lang3.ArrayUtils;

import org.idw.protocol.keyence.UpperLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class UpperLinkHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(UpperLinkHandler.class);
    @Override
    public void channelActive(final ChannelHandlerContext ctx) { // (1)
        final ByteBuf time = ctx.alloc().buffer(4); // (2)
        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
        UpperLink up = new UpperLink("kv-5000");
        HashMap<String,Object> opt = new HashMap<String,Object>();
        opt.put("registerType","DM");
        opt.put("registerIndex","100");
        opt.put("unit","uint16");
        opt.put("count",2);
        ArrayList<Byte> cmd = up.getReadCommand(opt);
        Byte[] list2 = new Byte[cmd.size()];
        byte[] cmdbyte = ArrayUtils.toPrimitive(cmd.toArray(list2));
        ByteBuf out = Unpooled.wrappedBuffer(cmdbyte);
        final ChannelFuture f = ctx.writeAndFlush(out); // (3)
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                assert f == future;
                log.info("初始化发送完成");
                // ctx.close();
            }
        });
    }
    private static volatile  int num=1;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf) msg;
        try {
            // 3030303034203030303034=00004 00004
            // 4f4b=OK
            // 4531=E1
            // TODO 异常码为 E0~E6
            log.info("收到消息 hex dump: {}", ByteBufUtil.hexDump(m));
            Thread.sleep(1000);
            UpperLink up = new UpperLink("kv-5000");
            HashMap<String,Object> opt = new HashMap<String,Object>();
            opt.put("registerType","DM");
            opt.put("registerIndex","100");
            opt.put("unit","uint16");
            opt.put("count",2);
            num++;
            opt.put("v_1",num);
            opt.put("v_2",num);
            ArrayList<Byte> cmd = up.getWriteCommand(opt);
            Byte[] list2 = new Byte[cmd.size()];
            byte[] cmdbyte = ArrayUtils.toPrimitive(cmd.toArray(list2));
           // byte[] bytes = ArrayUtils.toPrimitive(cmdBytes);
            // ByteBuf out = ctx.alloc().directBuffer(93);
            ByteBuf out = Unpooled.wrappedBuffer(cmdbyte);
//            out.writeShort(0);//Transaction ID  2
//            out.writeShort(0);//protocal id     2
//            out.writeShort(0);//msg len         2
//            out.writeByte(1);//slave id         1
//            out.writeByte(0x10);//function code    1
            //out.writeBytes();
//            final ChannelFuture f = ctx.channel().writeAndFlush(out);
//            // final ChannelFuture f = ctx.writeAndFlush("RDS FM.U100 1\r\n"); // (3)
//            f.addListener(new ChannelFutureListener() {
//                @Override
//                public void operationComplete(ChannelFuture future) {
//                    // assert f == future;
//                    log.info("消息接收并发送完成");
//                    // ctx.close();
//                }
//            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            m.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
