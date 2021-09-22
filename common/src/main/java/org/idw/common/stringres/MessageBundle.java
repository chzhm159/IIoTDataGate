package org.idw.common.stringres;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.ResourceBundle;

public class MessageBundle {
    private String path = "";
    private Locale locale =Locale.getDefault();
    private ResourceBundle rb =null;
    public MessageBundle(String path,Locale locale){
        this.path = path;
        this.locale = locale;
        rb = ResourceBundle.getBundle(this.path, locale);
    }
    public String getString(String key,String defaultValue,String... args){
        if(defaultValue==null || StringUtils.isEmpty(defaultValue)){
            defaultValue="null";
        }
        if(rb.containsKey(key)){
            return  rb.getString(key);
        }else{
            return defaultValue;
        }
    }
}
