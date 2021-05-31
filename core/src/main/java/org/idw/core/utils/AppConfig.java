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
    public static String getValueFromMap(String key,HashMap<String,Object> cfgMap){
        if(StringUtils.isEmpty(key)) return null;
        String[] ks = StringUtils.split(key,'.');
        if(ks==null || ks.length==0) return null;
        HashMap<String,Object> nv = null;
        String value = null;
        if(cfgMap==null) cfgMap=getConfig();
        for (String kn : ks) {
            if(nv==null) nv=cfgMap;
            Object valueObj = nv.get(kn);
            if (valueObj instanceof Map) {
                nv = (HashMap<String, Object>) valueObj;
            }else if(valueObj instanceof String){
                value = (String)valueObj;
                break;
            }
        }
        return value;
    }
    public static HashMap<String,Object> getConfig(){
        HashMap<String,Object> config = getInstance().cfg;
        AppConfig inst = getInstance();
        if(inst.cfg==null){
            inst.cfg = inst.load("config/config.yml");
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
            HashMap<String,Object> configMap = yaml.load(input);
            return configMap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * <p> 将 key 中的占位符替换为 config.yml 配置文件中 env 下同名属性的值. 例如: </p>
     *
     * <p> 字符串: 'config/${tagsfile}.json' </p>
     * <p> config.yml文件中 env: </p>
     * <p> &nbsp; &nbsp; tagsfile: 'abc' </p>
     *
     * <p>则返回 config/abc.json </p>
     * @param tpl 包含 ${xxx} 格式占位符的字符串
     * @param values 如果为 null,则使用 env 中的配置
     * @return
     */
    public static String resolveString(String tpl, HashMap<String,Object> values){
        if(values==null){
            HashMap<String, Object> cfg = getConfig();
            if(cfg==null)return null;
            HashMap<String, Object> env = (HashMap<String, Object>)cfg.get("env");
            values=env;
        }
        StringSubstitutor sub = new StringSubstitutor(values);
        return sub.replace(tpl);
    }
}
