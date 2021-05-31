package org.idw.core.model;

import org.idw.core.utils.AcquireTagsDefine;
import org.idw.core.utils.TagsDefineFileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Optional;

public class DeviceManager {
    private static final Logger log = LoggerFactory.getLogger(DeviceManager.class);
    private HashMap<String,Device> devList = new HashMap<>();

    private static  DeviceManager instance = null;
    private DeviceManager(){}
    public static DeviceManager getInstance() {
        if(instance == null){
            //创建实例之前可能会有一些准备性的耗时工作
            // Thread.sleep(300);
            synchronized (DeviceManager.class) {
                if(instance == null){//二次检查
                    instance = new DeviceManager();
                }
            }
        }
        return instance;
    }
    public void addDevice(Device dev){
        if(devList.containsKey(dev.getDeviceID())) {
            log.warn("已添加相同 deviceID 的配置 {}",dev.getDeviceID());
            return ;
        }
        devList.put(dev.getDeviceID(),dev);
    }
    public HashMap<String,Device> getDevices(){
        return this.devList;
    }

    public Device getDeviceByHostPort(String host,int port){
        Optional<Device> devOpt = this.devList.values().stream().filter(dev -> {
            String h = dev.getHost();
            int p = dev.getPort();
            return (host.equalsIgnoreCase(h) && port == p);
        }).findFirst();
        if(devOpt.isPresent()){
            return devOpt.get();
        }else{
            return null;
        }
    }
}
