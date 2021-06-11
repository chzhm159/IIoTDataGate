package org.idw.core.bootconfig;

import io.netty.channel.Channel;

/**
 * 针对不同的协议配置不同的 channelHandler
 */
public abstract class ProtocolAssembler {
    public abstract void initHandler(Channel ch);
}
