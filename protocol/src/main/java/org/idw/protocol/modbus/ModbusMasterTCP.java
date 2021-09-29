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
package org.idw.protocol.modbus;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import org.idw.common.stringres.MessageResources;
import org.idw.protocol.AbstractProtocol;
import org.idw.protocol.modbus.requests.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;


public class ModbusMasterTCP extends AbstractProtocol {
    private static final Logger log = LoggerFactory.getLogger(ModbusMasterTCP.class);
    private ModbusTcpCodec modbusTcpCodec;
    public ModbusMasterTCP(){
        ModbusRequestEncoder encoder = new ModbusRequestEncoder();
        ModbusRequestDecoder decoder= new ModbusRequestDecoder();
        modbusTcpCodec = new ModbusTcpCodec(encoder,decoder);
    }
    @Override
    public ByteBuf read(HashMap<String, Object> args) {

        int count = getReadCount(args);
        int idx = getReadIndex(args);
        short tid = getTransactionId(args);
        short uid = getUnitId(args);
        ModbusRequest pdu = getReadPDU(args,idx,count);
        if(count==-1||idx==-1||tid==-1||uid==-1 || pdu==null){
           return null;
        }

        ModbusTcpPayload modbusTcpPayload = new ModbusTcpPayload(tid,uid,pdu);
        // 读取数据类型
        Object unitObj = args.get("unit");
        ByteBuf buffer = Unpooled.buffer();
        modbusTcpCodec.encode(modbusTcpPayload,buffer);
        String dump = ByteBufUtil.prettyHexDump(buffer);
        log.debug("测试命令构造: {}",dump);
        return buffer;
    }

    private ModbusRequest getReadPDU(HashMap<String, Object> args,int index,int count){
        // 可访问的4个区域 线圈状态(Coils),离散输入(DiscreteInput),保持寄存器(HoldingRegisters),输入寄存器(InputRegister)
        ModbusRequest pdu = null;
        if(!args.containsKey("registerType")){
            String err = MessageResources.getMessage("error.read.nofc","必须指定 功能码");
            log.error(err);
            return pdu;
        }
        String funCode = args.get("registerType").toString().toLowerCase();
        try {
            pdu = forRead(funCode,index,count);
        } catch (Exception e) {
            String err = MessageResources.getMessage("error.read.invalidfc","暂未支持的 功能码");
            log.error(err);
        }
        return pdu;
    }
    private int getReadIndex(HashMap<String, Object> args){
        int idx = -1;
        if(!args.containsKey("registerIndex")){
            String noidx = MessageResources.getMessage("error.read.noidx","必须指定 读取索引号");
            log.error(noidx);
            return idx;
        }
        // 读取 地址编号
        Object index = args.get("registerIndex");
        try{
            idx = Integer.parseInt(index.toString());
        }catch (Exception e){
            String idxErr = MessageResources.getMessage("error.read.invalididx","读取的索引号必须为数字");
            log.error("{}:[{}]",idxErr,index);
        }
        return idx;
    }
    private int getReadCount(HashMap<String, Object> args){
        // 读取数量
        int c = -1;
        if(!args.containsKey("count")){
            String nocount = MessageResources.getMessage("error.read.nocount","必须指定 读取个数");
            log.error(nocount);
            return c;
        }

        Object count = args.get("count");
        try{
            c = Integer.parseInt(count.toString());
        }catch (Exception e){
            String err = MessageResources.getMessage("error.read.invalididx","读取的索引号必须为数字");
            log.error("{}:[{}]",err,count);
        }
        return c;
    }
    private short getTransactionId(HashMap<String, Object> args){
        short tid = -1;
        if(!args.containsKey("transactionId")){
            String notid = MessageResources.getMessage("error.read.notid","必须指定 会话Id");
            log.error(notid);
            return tid;
        }
        Object transactionId = args.get("transactionId");
        try{
            tid = Short.parseShort(transactionId.toString());
        }catch (Exception e){
            String err = MessageResources.getMessage("error.read.invalidtid","会话ID不能大于256");
            log.error("{}:[{}]",err,transactionId);
        }
        return tid;
    }
    private short getUnitId(HashMap<String, Object> args){
        short uid = -1;
        if(!args.containsKey("unitId")){
            String nouid = MessageResources.getMessage("error.read.nouid","必须指定 设备单元号");
            log.error(nouid);
            return uid;
        }
        Object transactionId = args.get("unitId");
        try{
            uid = Short.parseShort(transactionId.toString());
        }catch (Exception e){
            String err = MessageResources.getMessage("error.read.invaliduid","设备单元号不能大于128");
            log.error("{}:[{}]",err,transactionId);
        }
        return uid;
    }
    @Override
    public ByteBuf write(HashMap<String, Object> args) {
        return null;
    }
    public ModbusRequest forRead(String funCode,int addr,int quantity) throws Exception {
        switch(funCode) {
            case "readcoils" : return decodeReadCoils(addr,quantity);
            case "readdiscreteinputs": return  decodeReadDiscreteInputs(addr,quantity);
            case "readholdingregisters":return decodeReadHoldingRegisters(addr,quantity);
            case "readinputregisters": return decodeReadInputRegisters(addr,quantity);
            case "readwritemultipleregisters": return decodeReadWriteMultipleRegisters(addr,quantity,0,0,null) ;
            default:
                throw new Exception("IllegalFunctionCode");
        }
    }
    public ModbusRequest forWrite(String funCode,int addr,int quantity,ByteBuf value) throws Exception {
        switch(funCode) {
            case "readwritemultipleregisters": return decodeReadWriteMultipleRegisters(addr,quantity,0,0,null) ;
            case "writesinglecoil": return decodeWriteSingleCoil(addr,value);
            case "writesingleregister": return decodeWriteSingleRegister(addr,value);
            case "writemultiplecoils": return decodeWriteMultipleCoils(addr,quantity,value);
            case "writemultipleregisters": return decodeWriteMultipleRegisters(addr,quantity,value);
            case "maskwriteregister": return decodeMaskWriteRegister(addr,quantity,value);
            default:
                throw new Exception("IllegalFunctionCode");
        }
    }
    /*
暂未支持
              case "readexceptionstatus": return fromCode(0x07);
//            case "diagnostics": return fromCode(0x08);
//            case "getcommeventcounter": return fromCode(0x0B);
//            case "getcommeventlog": return fromCode(0x0C);
//            case "reportslaveid": return fromCode(0x11);
//            case "readfilerecord": return fromCode(0x14);
//            case "writefilerecord": return fromCode(0x15);
//            case "readfifoqueue": return fromCode(0x18) ;
//            case "encapsulatedinterfacetransport": return fromCode(0x2B) ;
    * */
    private ReadCoilsRequest decodeReadCoils(int address,int quantity) {
        return new ReadCoilsRequest(address, quantity);
    }

    private ReadDiscreteInputsRequest decodeReadDiscreteInputs(int address,int quantity) {
        return new ReadDiscreteInputsRequest(address, quantity);
    }

    private ReadHoldingRegistersRequest decodeReadHoldingRegisters(int address,int quantity) {

        return new ReadHoldingRegistersRequest(address, quantity);
    }

    private ReadInputRegistersRequest decodeReadInputRegisters(int address,int quantity) {
        return new ReadInputRegistersRequest(address, quantity);
    }

    private WriteSingleCoilRequest decodeWriteSingleCoil(int address,ByteBuf value) {
        boolean v = value.readBoolean();
        return new WriteSingleCoilRequest(address, v);
    }

    private WriteSingleRegisterRequest decodeWriteSingleRegister(int address,ByteBuf buffer) {
        int value = buffer.readUnsignedShort();
        return new WriteSingleRegisterRequest(address, value);
    }

    private WriteMultipleCoilsRequest decodeWriteMultipleCoils(int address,int quantity,ByteBuf values) {
//        int address = buffer.readUnsignedShort();
//        int quantity = buffer.readUnsignedShort();
//        int byteCount = buffer.readUnsignedByte();
//        ByteBuf values = buffer.readSlice(quantity).retain();

        return new WriteMultipleCoilsRequest(address, quantity, values);
    }

    private WriteMultipleRegistersRequest decodeWriteMultipleRegisters(int address,int quantity,ByteBuf values) {
//        int address = buffer.readUnsignedShort();
//        int quantity = buffer.readUnsignedShort();
//        int byteCount = buffer.readUnsignedByte();
//        ByteBuf values = buffer.readSlice(byteCount).retain();

        return new WriteMultipleRegistersRequest(address, quantity, values);
    }

    private MaskWriteRegisterRequest decodeMaskWriteRegister(int address, int andMask, ByteBuf values) {
//        int address = buffer.readUnsignedShort();
//        int andMask = buffer.readUnsignedShort();
        int orMask = values.readUnsignedShort();
        return new MaskWriteRegisterRequest(address, andMask, orMask);
    }

    private ReadWriteMultipleRegistersRequest decodeReadWriteMultipleRegisters(int readAddress, int readQuantity, int writeAddress, int writeQuantity,
                                                                               ByteBuf values) {
        return new ReadWriteMultipleRegistersRequest(readAddress, readQuantity, writeAddress, writeQuantity, values);
    }
}
