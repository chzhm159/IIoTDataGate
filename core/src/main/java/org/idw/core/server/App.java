package org.idw.core.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.apache.commons.lang3.StringUtils;
import org.idw.core.model.*;
import org.idw.core.testanddemo.UpperLinkHandler;
import org.idw.core.utils.AppConfig;
import org.idw.core.utils.TagsDefineFileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    private static final String class_method_regex = "^[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+[0-9a-z_]+#([a-z_]+[0-9a-z_]+)+";

    private static final Pattern class_method_pattern = Pattern.compile(class_method_regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    public static void main(String[] args) {

        //AcquireTagsDefine atd = start();
        //InitDevicesList(atd);

        HashMap<String, Object> cfg = AppConfig.getConfig();
        HashMap<String, Object> cfg1 = AppConfig.getConfig();
        log.debug("配置文件内容: {}", cfg1);
        log.debug("app.run.path={}", AppConfig.getValueFromMap("app.run.path", cfg));
        log.debug("data_cache.redis.port={}", AppConfig.getValueFromMap("data_cache.redis.port"));
        log.debug("resolveString={}", AppConfig.resolveString("config/${tagsfile}.json", null));
        log.debug("app.name={}", ((HashMap<String, Object>) cfg.get("app")).get("name"));
        AcquireTagsDefineModel definfo = start();
        deviceParser(definfo);
        BootstrapManager bootManager = BootstrapManager.getInstance();
        DeviceManager devManager = DeviceManager.getInstance();
        bootManager.setUp(devManager);
    }

    public static AcquireTagsDefineModel start() {
        String tagFilePath = AppConfig.getValueFromMap("app.tags", null);
        String trueTagFilePath = AppConfig.resolveString(tagFilePath, null);
        TagsDefineFileProcessor tdfp = new TagsDefineFileProcessor();
        AcquireTagsDefineModel atd = tdfp.load(trueTagFilePath);
        log.info("采集定义文件已加载 {}", atd);
        return atd;
    }

    public static void deviceParser(AcquireTagsDefineModel tagDefs) {
        DeviceManager devManager = DeviceManager.getInstance();
        ArrayList<DeviceDefineModel> devList = tagDefs.getDevices();
        devList.forEach(devDef -> {
            Device dev = deviceDefineConvert2Device(devDef);
            devManager.addDevice(dev);
        });
    }

    public static Device deviceDefineConvert2Device(DeviceDefineModel devDef) {
        Device dev = new Device();
        // 设备ID
        String devID = devDef.getDeviceID();
        dev.setDeviceID(devID);

        // 设备名称
        String devName = devDef.getDeviceName();
        dev.setDeviceName(devName);

        // 设备IP
        String devHost = devDef.getHost();
        dev.setHost(devHost);

        // 设备端口号
        int devPort = devDef.getPort();
        dev.setPort(devPort);

        // 设备名称
        String devProtocol = devDef.getProtocolType();
        dev.setProtocolType(devProtocol);

        // 设备型号
        String devModel = devDef.getDeviceModel();
        dev.setDeviceModel(devModel);

        // 设备编号
        String devCode = devDef.getDeviceCode();
        dev.setDeviceCode(devCode);

        // 链接超时设置
        int devCT = devDef.getConnectTimeout();
        dev.setConnectTimeout(devCT);

        // 断线失败重连的间隔
        int devRT = devDef.getRetryInterval();
        dev.setRetryInterval(devRT);


        ArrayList<TagDefineModel> tagList = devDef.getTags();
        tagList.forEach(tagDef -> {
            Tag tag = tagDefineConvert2Tag(tagDef);
            tag.setDevice(dev);
            dev.addTag(tag);
        });
        return dev;
    }

    /**
     * 将 配置文件中定义的 tag 转换为 采集过程中的 Tag 对象
     * TODO
     * 1. 需要实现当采集到的数值的后如何处理的函数上(反射?还是使用库?)
     * 2. Unit 类型的定义
     *
     * @param tagDef
     * @return
     */
    public static Tag tagDefineConvert2Tag(TagDefineModel tagDef) {
        Tag tag = new Tag();
        String key = tagDef.getKey();
        tag.setKey(key);

        String name = tagDef.getTagName();
        tag.setTagName(name);

        String regType = tagDef.getRegisterType();
        tag.setRegisterType(regType);

        int index = tagDef.getRegisterIndex();
        tag.setRegisterIndex(index);

        int offset = tagDef.getOffset();
        tag.setOffset(offset);

        String unit = tagDef.getUnit();
        tag.setUnit(unit);

        int count = tagDef.getCount();
        tag.setCount(count);

        int RInterval = tagDef.getReadInterval();
        tag.setReadInterval(RInterval);

        long RTimeout = tagDef.getTimeout();
        tag.setTimeout(RTimeout);

        String operate = tagDef.getOperate();
        tag.setOperate(operate);

//        String operate = tagDef.getOperate();
//        tag.setDataStrategy(operate);

        String clazzDef = tagDef.getValueHandler();
        Matcher match = class_method_pattern.matcher(clazzDef);
        // 这里处理的潜台词是 每个 Tag 变量对应一个实例,虽然可以将多个变量指定为同一个类
        // 但却是处在不同的对象中,如果面对多个变量之间互相关联的场景中,是不方便处理的
        if (match.matches()) {
            if (!StringUtils.isEmpty(clazzDef)) {
                try {
                    String className = clazzDef.substring(0, clazzDef.indexOf("#"));
                    String methodName = clazzDef.substring(clazzDef.indexOf("#") + 1);
                    Class<?> handlerClazz = Class.forName(className);
                    Object inst = handlerClazz.newInstance();
                    Method mtd = handlerClazz.getMethod(methodName, Tag.class, Object.class);
                    tag.setInstance(inst);
                    tag.setValueHandlerMethod(mtd);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("变量[{}]定义的 ValueHandler 函数绑定异常: ", e.getStackTrace().toString());
                }
            } else {
                log.warn("变量[{}] 未指定 ValueHandler 跳过函数绑定.", key);
            }
        } else {
            log.error("变量[{}]定义的 ValueHandler 格式错误,不符合 Java 包路径和方法命名规范.跳过函数绑定", key);
        }

        return tag;
    }

    public static void initDevicesList(AcquireTagsDefineModel tagDefs) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.TCP_NODELAY, true);
            b.option(ChannelOption.SO_LINGER, 1);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addFirst(new LineBasedFrameDecoder(2048, true, false));
                    ch.pipeline().addLast(new UpperLinkHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect("192.168.0.40", 8501).sync(); // (5)
            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // workerGroup.shutdownGracefully();
        }
    }
}
