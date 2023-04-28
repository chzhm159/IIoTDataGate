package org.idw.core.model;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Tag数据对象,读取到的数据,或者需要写入的数据都用此对象来表示;
 * 通过此对象来实现各个数据类型的转换
 */
public class TagValue {
    private String tagKey;

    private String valueString;

    // 实际是 ByteBuf 类型.
    private Object rawData;

    /**
     * 返回此值对应的变量Key
     * @return
     */
    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public Object getRawData() {
        return rawData;
    }

    public void setRawData(Object rawData) {
        this.rawData = rawData;
    }
    public void setShort(short v){
        ByteBuf databuf = Unpooled.buffer(2);
        databuf.writeShort(v);
        setRawData(databuf);
    }
    public void setInt(int v){
        ByteBuf databuf = Unpooled.buffer(4);
        databuf.writeShort(v);
        setRawData(databuf);
    }
    public int getIntValue(){
        if(rawData!=null){
            ByteBuf data = (ByteBuf)rawData;
            return data.getInt(0);
        }else{
            return 0;
        }
    }
}
