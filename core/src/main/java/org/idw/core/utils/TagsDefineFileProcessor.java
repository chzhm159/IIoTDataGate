package org.idw.core.utils;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class TagsDefineFileProcessor {
    private static final Logger log = LoggerFactory.getLogger(TagsDefineFileProcessor.class);

    public AcquireTagsDefine load(String tagsPath){
        File tagFile = FileUtils.getFile(tagsPath);
        log.debug("读取 {}, 绝对路径 {}",tagsPath,tagFile.getAbsolutePath());
        try {
            AcquireTagsDefine bean = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readerFor(AcquireTagsDefine.class)
                    .readValue(tagFile);
            return  bean;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
