package org.idw.core.bootconfig;

import org.idw.core.model.Device;
import org.idw.core.model.DeviceManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeScheduler {
    private static final Logger log = LoggerFactory.getLogger(TimeScheduler.class);
    private static  final int threadPoolSize=1;
    private static  final int threadPriority=5;

    private static TimeScheduler instance = null;
    private TimeScheduler(){}
    public static TimeScheduler getInstance() {
        if(instance == null){
            //创建实例之前可能会有一些准备性的耗时工作
            // Thread.sleep(300);
            synchronized (DeviceManager.class) {
                if(instance == null){//二次检查
                    instance = new TimeScheduler();
                }
            }
        }
        return instance;
    }

    public Scheduler getScheduler(Device device){
        try {
            Scheduler scheduler = DirectSchedulerFactory.getInstance().getScheduler(device.getDeviceID());
            if(scheduler!=null){
                return scheduler;
            }else
            {
                DirectSchedulerFactory.getInstance().createScheduler(
                        device.getDeviceID(), // instance-name
                        device.getDeviceID(), // instance-id
                        new SimpleThreadPool(threadPoolSize,threadPoolSize),
                        new RAMJobStore()
                );
                return DirectSchedulerFactory.getInstance().getScheduler(device.getDeviceID());
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            return null;
        }
    }
}
