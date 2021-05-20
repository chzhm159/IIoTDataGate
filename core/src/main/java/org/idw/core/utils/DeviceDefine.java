package org.idw.core.utils;

import java.util.ArrayList;

public class DeviceDefine {
    // 设备唯一标识符
    private String  deviceID;
    // 设备名称
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
    private ArrayList<TagDefine> tags = new ArrayList<TagDefine>();

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

    public ArrayList<TagDefine> getTags() {
        return tags;
    }

    public void setTags(ArrayList<TagDefine> tags) {
        this.tags = tags;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
}
