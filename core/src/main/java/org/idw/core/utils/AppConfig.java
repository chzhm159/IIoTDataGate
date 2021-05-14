package org.idw.core.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.idw.core.model.ConfigModel;
import org.idw.core.model.DeviceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    private static  AppConfig instance = null;
    private HashMap<String,Object> cfg = null;

    private AppConfig(){}

    public static AppConfig getInstance() {
        if(instance == null){
            //创建实例之前可能会有一些准备性的耗时工作
            // Thread.sleep(300);
            synchronized (DeviceManager.class) {
                if(instance == null){//二次检查
                    instance = new AppConfig();
                }
            }
        }
        return instance;
    }

    public static HashMap<String,Object> getConfig(String path){
        HashMap<String,Object> config = getInstance().cfg;
        AppConfig inst = getInstance();
        if(inst.cfg==null){
            if(StringUtils.isEmpty(path)){
                path="config/config.yml";
            }
            inst.cfg = inst.load(path);
        }
        return inst.cfg;
    }
    private HashMap<String,Object> load(String cfgPath){
        Yaml yaml = new Yaml();
        try {
            File cf = new File(cfgPath);
            if(!cf.exists()){
                return null;
            }
            InputStream input = new FileInputStream(cf);
            Iterable<Object> configMap = yaml.loadAll(input);
            for (Object data :configMap) {
                LinkedHashMap<String, Object> item = (LinkedHashMap<String, Object>) data;
                log.info("配置信息 {}",data);
            }
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    public String resolveString(String tpl, HashMap<String,Object> values){
        StringSubstitutor sub = new StringSubstitutor(values);
        return sub.replace(tpl);
    }
}
