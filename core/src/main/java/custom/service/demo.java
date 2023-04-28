package custom.service;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.StringUtils;
import org.idw.core.model.Tag;
import org.idw.core.model.TagValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * 注意: 每个 Tag 对应本类 一个实例,意味着方法虽然是写在同一个类中,但是实例属性不共享
 */
public class demo {

    private static final Logger log = LoggerFactory.getLogger(demo.class);

    // 用于演示 每个函数是分属不同的实例中
    private  String inistNanme="none";

    public void onOutput(Tag tag){
        inistNanme = this.hashCode()+"___onOutput";
        ByteBuf value = (ByteBuf)tag.getTagValue().getRawData();
        // 上位链路是 ASCII 格式的协议,所以将结果直接转换为 ascii 再处理
        String vStr = value.toString(Charset.forName("ascii"));
        int count = tag.getCount();
        // log.debug("{} onOutput 变量[{}],数据接收回调已正常调用: {}={}",inistNanme,tag.getKey(),ByteBufUtil.hexDump(value),vStr);
        // 上位链路协议中,如果返回错误数据,是已 E 开头的
        boolean err = StringUtils.startsWithIgnoreCase(vStr,"E");
        if(err){
            log.error("返回错误数据:{}",vStr);
            return ;
        }
        // 上位链路协议中,如果返回多个数据,是以空格分开
        String[] values = vStr.split(" ");
        String unitName = tag.getUnit();
        for (String vv : values) {
            // 因为ASCII 所以基本上就是用字符串转换为特定类型. 后续可能考虑 框架层面处理这个问题,但是需要自行处理异常
            // 16位整数
            Short v = Short.parseShort(vv);
            // 32位浮点数
            // Float.valueOf(vv);
            // 32位整数
            // Integer.valueOf(vv);
            log.debug("onOutput 变量[{}]={}",tag.getTagName(),v);
        }
        TagValue data = new TagValue();
        data.setTagKey(tag.getKey());
        data.setRawData("123 456");
        // tag.write(data);
    }

    public void onBad(Tag tag){
        inistNanme = this.hashCode()+"___onBad";
        ByteBuf value = (ByteBuf)tag.getTagValue().getRawData();
        // 上位链路是 ASCII 格式的协议,所以将结果直接转换为 ascii 再处理
        String vStr = value.toString(Charset.forName("ascii"));
        int count = tag.getCount();
        //log.debug("{} 2个不良 变量[{}],数据接收回调已正常调用: {}={}",inistNanme,tag.getKey(),ByteBufUtil.hexDump(value),vStr);
        // 上位链路协议中,如果返回错误数据,是已 E 开头的
        boolean err = StringUtils.startsWithIgnoreCase(vStr,"E");
        if(err){
            log.error("返回错误数据:{}",vStr);
            return ;
        }
        // 上位链路协议中,如果返回多个数据,是以空格分开
        String[] values = vStr.split(" ");
        String unitName = tag.getUnit();
        for (String vv : values) {
            // 因为ASCII 所以基本上就是用字符串转换为特定类型. 后续可能考虑 框架层面处理这个问题,但是需要自行处理异常
            // 16位整数
            Short v = Short.parseShort(vv);
            // 32位浮点数
            // Float.valueOf(vv);
            // 32位整数
            // Integer.valueOf(vv);
            log.debug("2个不良 变量[{}]={}",tag.getTagName(),v);
        }
        //log.debug("{} onBad 变量[{}],数据接收回调已正常调用: {}",inistNanme,tag.getKey(),ByteBufUtil.hexDump(value));
    }
}
