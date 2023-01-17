package org.idw.core.model;

import com.google.common.eventbus.Subscribe;

public class DeviceEvent {
    private Device device;
    public DeviceEvent(Device dev){
        this.device = dev;
    }

    /**
     * 变量写入事件
     * @param data
     */
    @Subscribe
    public void tagWrite(TagValue data){
        device.write(data);
    }
}
