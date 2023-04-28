package org.idw.core.cache;

import org.idw.core.model.Tag;
import org.idw.core.model.TagValue;

/**
 * 首先不要批评为啥不用Junit,暂时没时间写
 */
public class DataCacheTest {
    public static void main(String[] args){
        DataCache dc = DataCache.getInstance();
        dc.init();
        Tag tag_1 = new Tag();
        String key = "l1:d1:t1";
        tag_1.setKey(key);
        String name = "output";;
        tag_1.setTagName(name);
        TagValue tv_string = new TagValue();
        tv_string.setRawData(123);
        tag_1.setTagValue(tv_string);
        dc.save(tag_1);

        Tag tag_2 = new Tag();
        String key2 = "l1:d1:t2";
        tag_2.setKey(key2);
        String name2 = "bad";;
        tag_2.setTagName(name2);
        TagValue tv_int = new TagValue();
        tv_int.setRawData(20221212);
        tag_2.setTagValue(tv_int);
        dc.save(tag_2);

        Tag tag_3 = new Tag();
        String key3 = "l1:d1:t3";
        tag_3.setKey(key3);
        String name3 = "state";;
        tag_3.setTagName(name3);
        TagValue tv_double = new TagValue();
        tv_double.setRawData(123.456);
        tag_3.setTagValue(tv_double);
        dc.save(tag_3);

    }
}
