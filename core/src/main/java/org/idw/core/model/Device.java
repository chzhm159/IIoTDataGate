package org.idw.core.model;

import com.sun.org.apache.xpath.internal.operations.Bool;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelId;

import java.util.concurrent.ConcurrentHashMap;

public class Device {
    // 设备唯一标识符
    private String  deviceID;

    // Channel ID
    private ChannelId cid;

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
    private ConcurrentHashMap<String ,Tag> tags = new ConcurrentHashMap<String, Tag>();
    // 当前设备对应的连接对象
    private ChannelFuture channelFutrue;

    /**
     * 设备的连接状态
     */
    public enum State {
        /**
         * 等待链接中
         */
        waiting,
        /**
         * 已连接
         */
        connected,
        /**
         * 已断开连接
         */
        disconnected
    }
    /**
     * 设备的连接状态
     */
    public static enum Protocols {
        /**
         * 基恩士PLC,上位链路协议
         */
        upperlink("upperlink"),
        /**
         * 西门子PLC,S7协议
         */
        s7("s7");
        private String name;

        private Protocols(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
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

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public ChannelFuture getChannelFuture()
    {
        return this.channelFutrue;
    }

    public void setChannelFuture(ChannelFuture channel)
    {
        this.channelFutrue = channel;
    }

    public ChannelId getChannelId() {
        return cid;
    }

    public void setChannelId(ChannelId cid) {
        this.cid = cid;
    }

    public boolean isConnected()
    {
        if(this.channelFutrue==null) return false;
        return this.channelFutrue.channel().isActive();
    }
}
