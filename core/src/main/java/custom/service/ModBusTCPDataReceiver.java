package custom.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.idw.core.model.Tag;
import org.idw.core.model.TagData4Write;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class ModBusTCPDataReceiver {
    private static final Logger log = LoggerFactory.getLogger(demo.class);
    public void onInValue(Tag tag, Object v){
        ByteBuf value = (ByteBuf)v;
        // log.debug("modebus 接收到数据:\n {}",ByteBufUtil.prettyHexDump(value));
        Tag wTag = tag.getDevice().getTag("L1_D1_bad");
        TagData4Write data = new TagData4Write();
        data.setTagKey(wTag.getKey());
        data.setCount(2);
        ByteBuf databuf = Unpooled.buffer(8);
        Random r = new Random();
        int a = r.nextInt(65526);
        int b = r.nextInt(65526);
        databuf.writeShort(a);
        databuf.writeShort(b);
        data.setData(databuf);
        wTag.write(data);
        // wTag.write();
    }

    public void onReadAndWrite(Tag tag, Object v){
        ByteBuf value = (ByteBuf)v;
        log.debug("[{}]:接收到数据:\n {}",tag.getKey(),ByteBufUtil.prettyHexDump(value));
        TagData4Write data = new TagData4Write();
        data.setTagKey(tag.getKey());
        data.setCount(2);
        ByteBuf databuf = Unpooled.buffer(8);
        databuf.writeShort(8);
        databuf.writeShort(9);
        data.setData(databuf);
        // tag.write(data);
    }
}
