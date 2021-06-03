package org.idw.core.bootconfig;

import io.netty.channel.Channel;

public abstract class ProtocolAssembler {
    public abstract void initHandler(Channel ch);
}
