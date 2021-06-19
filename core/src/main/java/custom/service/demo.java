package custom.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.idw.core.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class demo {

    private static final Logger log = LoggerFactory.getLogger(demo.class);

    public void onOutput(Tag tag,ByteBuf value){
        log.debug("onOutput 变量[{}],数据接收回调已正常调用: {}",tag.getKey(),ByteBufUtil.hexDump(value));
    }

    public void onBad(Tag tag, ByteBuf value){
        log.debug("onBad 变量[{}],数据接收回调已正常调用: {}",tag.getKey(),ByteBufUtil.hexDump(value));
    }
}
