package org.idw.core.model;

/**
 * 点位数据对象,读取到的数据,或者需要写入的数据都用此对象来表示
 */
public class TagValue {
    private String tagKey;

    private String valueString;

    // 目前没有想好, 是 ByteBuf 类型.
    private Object data;

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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
