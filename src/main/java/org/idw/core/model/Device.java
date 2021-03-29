package org.idw.core.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Device {
    // 折兵名称
    private String  deviceName;
    // 通讯协议编号
    private String protocolType;
    // 设备地址
    private String host;
    // 设备的通信端口号
    private int port;
    // 链接超时,单位 毫秒
    private int connectTimeout;
    // 重试时间间隔,单位 毫秒
    private int retryInterval;
    //
    private ConcurrentHashMap<String ,Tag> tags = new ConcurrentHashMap<String, Tag>();

    public void addTag(Tag t){
        tags.put(t.getKey(),t);
    }
    public boolean tagExists(String tk){
        return tags.containsKey(tk);
    }
    public Tag getTag(String tk){
        return tags.get(tk);
    }
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }
}
