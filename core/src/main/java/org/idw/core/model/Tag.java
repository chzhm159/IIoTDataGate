package org.idw.core.model;

import com.google.common.eventbus.AsyncEventBus;
import io.netty.buffer.ByteBuf;
import org.idw.core.cache.DataCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class Tag {
    private static final Logger log = LoggerFactory.getLogger(Tag.class);
    private Device device;
    // 变量名称,方便使用者记忆
    private String tagName;
    // 变量的key,使用 : 分割,可以定义任意层级,但必须全局唯一 root:level1:name
    private String key;
    // 当前变量对应的寄存器区域名称,例如 DM,或者FM
    private String registerType;
    // 会话id
    private String transactionId;

    // 寄存区编号
    private int registerIndex;
    // 相对寄存区编号的偏移量
    private int offset;
    // 操作数量
    private int count;
    // 变量数据类型
    private String unit;

    // 变量对应的数据
    private TagValue tagValue;

    // 采集周期,单位毫秒
    private int readInterval;
    // 读取超时,单位毫秒
    private long timeout;
    // 读取到数值后的处理函数
    private String valueHandler;
    // 读取次数,此变量优先与 loopRead, 即便是 loopRead 为true,则读取到指定次数后也会停止
    private int readTimes;
    // 是否循环读取
    private boolean loopRead;
    // value处理的实例
    private Object instance;
    // value处理的方法
    private Method valueHandlerMethod;
    // 主要用于处理变量的写入操作.防止在 onValueHandler 中 调用 tag.write 时造成死锁
    private AsyncEventBus eventBus;
    // 操作 r:只读,w:只写,rw:读写
    private String operate;
    // 读取指令
    private ByteBuf readCmd;
    // 写入指令
    private ByteBuf writeCmd;

    // 数据缓存层
    private DataCache dCache = null;

    public Tag() {
        dCache = DataCache.getInstance();
    }
    // 返回结果的处理方式: raw(默认方式,直接接受 ByteBuf 自行处理),后续会根据数据类型和数量,自动解析好后返回
    // decode
    // private String dataStrategy="raw";

    private HashMap context = new HashMap();

    /**
     * 获取当前Tag的扩展HashMap,可以用于存储一些额外信息
     *
     * @return
     */
    public HashMap getContext() {
        return context;
    }

    public void setContext(HashMap context) {
        this.context = context;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public void setEventBus(AsyncEventBus eb) {
        this.eventBus = eb;
    }

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


    public TagValue getTagValue() {
        return tagValue;
    }

    public void setTagValue(TagValue tagValue) {
        this.tagValue = tagValue;
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

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
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

    public ByteBuf getReadCmd() {
        readCmd.retain();
        return readCmd;
    }

    public void setReadCmd(ByteBuf cmd) {
        this.readCmd = cmd;
    }

    public String getOperate() {
        return operate;
    }

    public ByteBuf getWriteCmd() {
        return writeCmd;
    }

    public void setWriteCmd(ByteBuf writeCmd) {
        this.writeCmd = writeCmd;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }


    public void onValue(Object msg) {
        // StopWatch stopWatch = new StopWatch();
        // stopWatch.start();

        try {
            if(this.tagValue==null){
                TagValue tv = new TagValue();
                tv.setTagKey(this.key);
                this.tagValue = tv;
            }
            // 更新数据
            this.tagValue.setRawData(msg);
            // 保存到缓存中
            dCache.save(this);

            // 如果回调函数不为空,执行回调,未来考虑废弃
            if(valueHandlerMethod!=null){
                valueHandlerMethod.invoke(instance, this);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            log.error("变量[{}]处理异常1: {}", this.getKey(), e.getStackTrace().toString());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            log.error("变量[{}]处理异常2: {}", this.getKey(), e.getStackTrace().toString());
        }
        // stopWatch.stop();
        // log.debug("变量[{}],收到数据后耗时: {} 纳秒",this.getTagName(),stopWatch.getTime(TimeUnit.NANOSECONDS) );
    }

    public void write(TagValue data) {
        // 委托给 Device 来完成写入操作
        // 1. 避免读取到数据后写入另外一个变量时死锁问题
        // 2. Tag对象不持有链接操作对象
        eventBus.post(data);
    }
}
