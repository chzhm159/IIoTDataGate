package org.idw.common.stringres;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Locale;

public class MessageResources {
    private static final Logger log = LoggerFactory.getLogger(MessageResources.class);
    private static HashMap<String,MessageBundle> cache = new HashMap<String,MessageBundle>();

    /**
     * 获取本函数调用类对应的 .peroerties 文件中的字符串值
     * @param key
     * @param defValue
     * @param args     暂未支持,预计支持 string.template(format,args) 的功能
     * @return
     */
    public static String getMessage(String key,String defValue,String... args){
        StackTraceElement stact  = new Throwable().getStackTrace()[1];
        String caller = stact.getClassName();
        if(cache.containsKey(caller)){
            return cache.get(caller).getString(key,defValue);
        }else{
            try{
                MessageBundle mb = new MessageBundle(caller, Locale.getDefault());
                cache.put(caller,mb);
                return mb.getString(key,defValue);
            }catch (Exception e){
                log.error("{}_{}.properties not found",caller,Locale.getDefault().toString(),e);
                return defValue;
            }

        }
    }
}
