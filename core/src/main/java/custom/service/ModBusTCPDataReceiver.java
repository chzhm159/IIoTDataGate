package custom.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.idw.core.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModBusTCPDataReceiver {
    private static final Logger log = LoggerFactory.getLogger(demo.class);
    public void onInValue(Tag tag, ByteBuf value){
        log.debug("modebus 接收到数据:\n {}", ByteBufUtil.prettyHexDump(value));
    }
}
