/*
 * Copyright 2021 chzhm159
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.idw.protocol;

import io.netty.buffer.ByteBuf;


import java.util.HashMap;

/**
 * 通信协议的抽象,需要实现 <br/>
 * getReadCommand: 便于获取读取指令 <br/>
 * getWriteCommand 便于获取写入指令 <br/>
 * 目前此类的设计并不满意.基本要求是:<br/>
 * 1. 不要过度封装 <br/>
 * 2. 足够简洁 <br/>
 * 3. 扩展性好 <br/>
 *
 * 目前个人觉得不满意的点在于,代码在使用上比较繁琐,如果缺少文档的情况下,基本上无法正常使用.目前纠结:<br/>
 * 1. 重新设计接口参数 <br/>
 * 2. [个人偏向] 将错就错,后续通过增加各种便捷函数来弥补封装性不足的问题 <br/>
 * 3. 貌似没有解析TCP数据包的功能...
 */
public abstract class AbstractProtocol {
    /**
     * 设备型号标识符
     */
    protected String cpuModel;
    /**
     * 协议标识符
     */
    protected String protocol;

    /**
     * 根据参数构造一个读取指令,通常某个变量配置为读取指令后不会在变更配置,所以尽可能自己缓存返回的ByteBuf,
     * 以减少每次读取时重新构造的耗时
     * @param args 构造读取指令的参数列表,不同的协议参数内容不同
     * @return 读取指令的 Byte 数组表示形式
     */
    public abstract ByteBuf read(HashMap<String,Object> args);

    /**
     * 根据参数构造一个写入指令
     * @param args 构造写入指令的参数列表,不同的协议参数内容不同
     * @return 写入指令的 Byte 数组表示形式
     */
    public abstract  ByteBuf write(HashMap<String,Object> args);
}
