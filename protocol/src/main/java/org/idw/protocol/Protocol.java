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
 * encode: 协议的编码实现: 接收 Object 类型,(每个协议自行封装)解析为 io.netty.buffer.ByteBuf 因为最终还是基于Netty的,
 * 所以这里就不再纠结是否过于绑定Netty的问题了. <br/>
 * decode 协议的解码实现: 接收 io.netty.buffer.ByteBuf 返回Object(每个协议的封装)<br/>
 */
public abstract class Protocol {
    /**
     * 设备型号标识符
     */
    protected String cpuModel;
    /**
     * 协议标识符
     */
    protected String protocol;

    /**
     * 完成协议编码:
     * @param args 协议的封装对象
     * @return 返回 io.netty.buffer.ByteBuf
     */
    public abstract ByteBuf encode(Object args);

    /**
     * 完成协议解码
     * @param values 接收 io.netty.buffer.ByteBuf
     * @return 返回协议封装对象
     */
    public abstract Object decode(ByteBuf values);
}
