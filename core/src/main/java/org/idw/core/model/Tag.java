package org.idw.core.model;

import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.time.StopWatch;
import org.idw.core.utils.TagsDefineFileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class Tag {
    private static final Logger log = LoggerFactory.getLogger(Tag.class);
    // 变量名称,方便使用者记忆
    private String tagName;
    // 变量的key,使用 : 分割,可以定义任意层级,但必须全局唯一 root:level1:name
    private String key;
    // 当前变量对应的寄存器区域名称,例如 DM,或者FM
    private String registerType;
    // 寄存区编号
    private int registerIndex;
    // 相对寄存区编号的偏移量
    private int offset;
    // 操作数量
    private int count;
    // 变量数据类型
    private String unit;
    // 采集周期,单位毫秒
    private int readInterval;
    // 读取超时,单位毫秒
    private int readTimeout;
    // 读取到数值后的处理函数
    private String valueHandler;
    // 读取次数,此变量优先与 loopRead, 即便是 loopRead 为true,则读取到指定次数后也会停止
    private int readTimes;
    // 是否循环读取
    private boolean loopRead;

    private Object instance;
    private Method valueHandlerMethod;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }

    public int getRegisterIndex() {
        return registerIndex;
    }

    public void setRegisterIndex(int registerIndex) {
        this.registerIndex = registerIndex;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getReadInterval() {
        return readInterval;
    }

    public void setReadInterval(int readInterval) {
        this.readInterval = readInterval;
    }

    public String getValueHandler() {
        return valueHandler;
    }

    public void setValueHandler(String valueHandler) {
        this.valueHandler = valueHandler;
    }

    public int getReadTimes() {
        return readTimes;
    }

    public void setReadTimes(int readTimes) {
        this.readTimes = readTimes;
    }

    public boolean isLoopRead() {
        return loopRead;
    }

    public void setLoopRead(boolean loopRead) {
        this.loopRead = loopRead;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }


    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Method getValueHandlerMethod() {
        return valueHandlerMethod;
    }

    public void setValueHandlerMethod(Method valueHandlerMethod) {
        this.valueHandlerMethod = valueHandlerMethod;
    }

    public void onValue(Object msg){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            valueHandlerMethod.invoke(instance,this,msg);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        stopWatch.stop();
        log.debug("变量[{}],收到数据后耗时: {} 毫秒",this.getTagName(),stopWatch.getTime(TimeUnit.MILLISECONDS) );
    }
}
