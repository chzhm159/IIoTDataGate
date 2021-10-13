/*
 * Copyright 2016 Kevin Herron
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
/*
*   本程序基于 https://github.com/digitalpetri/modbus 感谢 Kevin Herron
*   1. 重新调整 package结构
*   2. 增加中文注释
* */
package org.idw.protocol.modbus;


public class ModbusTcpPayload {

    private final int transactionId;
    private final short unitId;
    private final ModbusPdu modbusPdu;

    /**
     * 构造一个基于tcp的数据包
     * 这里有个隐患,因为 Java 的short 是有 16位有符号整数,取值范围为   -32768 ~ 32767.
     * 假设现在有个需求需要读取 4万个 变量.那么极端情况下,transactionID 是会出现错误的.
     * 这里使用 Integer 来代替
     * @param transactionId
     * @param unitId
     * @param modbusPdu
     */
    public ModbusTcpPayload(int transactionId, short unitId, ModbusPdu modbusPdu) {
        this.transactionId = transactionId;
        this.unitId = unitId;
        this.modbusPdu = modbusPdu;
    }

    /**
     * 获取事务ID
     * @return
     */
    public int getTransactionId() {
        return transactionId;
    }

    /**
     * 获取 单元标识符 (串口链路或其他总线上远程终端标识)
     * @return
     */
    public short getUnitId() {
        return unitId;
    }

    /**
     * 获取 PDU
     * @return
     */
    public ModbusPdu getModbusPdu() {
        return modbusPdu;
    }

}
