package org.idw.core.server;

import org.idw.protocol.DataTypeNames;
import org.idw.core.utils.TagsDefineFileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    public static void main(String[] args) {
        

    }
    public static void start(){
        TagsDefineFileProcessor tdfp = new TagsDefineFileProcessor();
        tdfp.load("config/tags.json");
        log.info("加载plc协议 {}", DataTypeNames.int16);
    }
}
