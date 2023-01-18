package custom.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.idw.core.model.Tag;
import org.idw.core.model.TagValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class ModBusTCPDataReceiver {
    private static final Logger log = LoggerFactory.getLogger(demo.class);
    public void onInValue(Tag tag){
        ByteBuf value = (ByteBuf)tag.getTagValue().getData();
        log.debug("tag[{}] 接收到数据:\n {}",tag.getKey(),ByteBufUtil.prettyHexDump(value));
        Tag wTag = tag.getDevice().getTag("L1:D1:bad");
        TagValue data = new TagValue();
        data.setTagKey(wTag.getKey());
        ByteBuf databuf = Unpooled.buffer(4);
        Random r = new Random();
        int a = r.nextInt(65526);
        int b = r.nextInt(65526);
        databuf.writeShort(a);
        databuf.writeShort(b);
        data.setData(databuf);
        wTag.write(data);
    }

    public void onReadAndWrite(Tag tag){
        ByteBuf value = (ByteBuf)tag.getTagValue().getData();
        log.debug("[{}]:接收到数据:\n {}",tag.getKey(),ByteBufUtil.prettyHexDump(value));
//        TagData data = new TagData();
//        data.setTagKey(tag.getKey());
//        data.setCount(2);
//        ByteBuf databuf = Unpooled.buffer(8);
//        databuf.writeShort(8);
//        databuf.writeShort(9);
//        data.setData(databuf);
        // tag.write(data);
    }
}
