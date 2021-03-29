package org.idw.core.model;

public class DeviceManager {
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

}
