package org.idw.core.cache;

import org.apache.commons.lang3.StringUtils;
import org.idw.core.model.DeviceManager;
import org.idw.core.model.Tag;
import org.idw.core.utils.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataCache {
    private static final Logger log = LoggerFactory.getLogger(DeviceManager.class);

    private static DataCache instance = null;
    private DataCacheByRedis cache = new DataCacheByRedis();

    private DataCache(){}

    public static DataCache getInstance() {
        if(instance == null){
            synchronized (DataCache.class) {
                if(instance == null){
                    instance = new DataCache();
                }
            }
        }
        return instance;
    }

    /**
     * 目前没有想好定义哪些接口. 后续可能设计成可替换的形式,目前就先redis实现
     */
    // private void registerHandler(){}

    /**
     * 初始化
     */
    public void init(){
        String type = (String)AppConfig.getValueFromMap("data-cache.type");
        if(StringUtils.equals(type,"redis")){
            cache.init();
        }
    }

    /**
     * 保存数据到缓存池中
     * @param tag
     * @return
     */
    public boolean save(Tag tag){
        boolean suc=false;
        try{
            cache.save(tag);
            suc=true;
        }catch (Exception e){
            log.error("数据保存异常: [{}] ,{}",tag.getKey(),e.getStackTrace());
            suc=false;
        }
        return suc;
    }
    public void get(String tagId){

    }
}
