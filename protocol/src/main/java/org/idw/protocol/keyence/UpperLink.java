//package org.idw.protocol;
//
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.apache.commons.lang3.StringUtils;
//
//import java.util.HashMap;
//
//public class UpperLink extends AbstractProtocol{
//    private static final Logger log = LoggerFactory.getLogger(UpperLink.class);
//    // 不同的地址区域对应的最大地址编号
//    private HashMap<String,Integer> registerType_MaxRange = new HashMap<String,Integer>();
//    private HashMap<String,String> dataTypes = new HashMap<String,String>();
//    public UpperLink(String cpuModel){
//
//        this.cpuModel = cpuModel;
//        this.protocol=ProtocolNames.UpperLink;
//        initValidDataMemory(cpuModel);
//        initDataType();
//    }
//    @Override
//    public Byte[] getReadCommand(HashMap<String, Object> args) {
//        Object registerTypeObj = args.get("registerType");
//        Object registerIndexObj = args.get("registerIndex");
//        Object unitObj = args.get("unit");
//        Object countObj = args.get("count");
//        String type = null,unit=null;
//        int index =-1 , count=1;
//        if(registerTypeObj==null){
//            log.error("未指定寄存器区域名称,例如DM/FM,故无法构造读取指令");
//            return null;
//        }
//        if(registerIndexObj==null){
//            log.error("未指定寄存器区域编号,例如 100,故无法构造读取指令");
//            return null;
//        }
//        try{
//            index = Integer.valueOf(registerIndexObj.toString());
//            if(index < 0){
//                log.error("寄存器区域编号必须大于等于0.当前参数为:{}",index);
//                return null;
//            }
//        }catch (Exception e){
//            log.error("指定寄存器区域编号非数字类型 {} Error: {}",registerIndexObj.toString(),e.getMessage());
//            return null;
//        }
//
//        if(countObj==null){
//            log.warn("未指定将要读取多少个数据,默认读取 1 个单位");
//        }else{
//            try{
//                count = Integer.valueOf(countObj.toString());
//                if(count < 0){
//                    log.error("读取数量必须大于0.当前参数为:{}",count);
//                    return null;
//                }
//            }catch (Exception e){
//                log.error("读取数量必须为数字类型 {} Error: {}",countObj.toString(),e.getMessage());
//                return null;
//            }
//        }
//        if(unitObj==null){
//            log.error("未指定将要读取地址的数据类型,例如 .U,故无法构造读取指令");
//            return null;
//        }else{
//
//            if(!dataTypes.containsKey(unitObj.toString())){
//                log.error("不支持的数据类型{}",unitObj.toString());
//                return null;
//            }
//            unit = dataTypes.get(unitObj.toString());
//        }
//
//        type=(String)registerTypeObj;
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("RDS ");
//        stringBuilder.append(type);
//        stringBuilder.append(index);
//        stringBuilder.append(unit);
//        stringBuilder.append(" ");
//        stringBuilder.append(count);
//        stringBuilder.append("\r");
//        String cmd = stringBuilder.toString();
//        log.debug("read command: {}",cmd);
//        return new Byte[0];
//    }
//
//    @Override
//    public Byte[] getWriteCommand(HashMap<String, Object> args) {
//        return new Byte[0];
//    }
//
//    private static boolean DataMemoryValid(String registerType){
//        return true;
//    }
//    public  static void main(String[] args){
//        // UpperLink.getReadCommand("FM",100,"UINT16");
//        UpperLink up = new UpperLink("kv-5000");
//        HashMap<String,Object> opt = new HashMap<String,Object>();
//        opt.put("registerType","DM");
//        opt.put("registerIndex","100");
//        opt.put("unit","uint16");
//        opt.put("count",2);
//        up.getReadCommand(opt);
//    }
//    private void initValidDataMemory(String cpu){
//        // TODO 根据型号,初始化对应的可读写区域.但是目前先不处理
//        //  同时设定最大取值范围,参考 <KvsEtherNetIP.pdf> 500 页 左右
//        // 目前参考资料仅限 kv-3000/5000/5500/7300/7500,而且无法全面测试
//        // 所以先完成 DM,EM,FM 区的测试,其他区域暂时无法测试
////        registerList.put("R",true);
////        registerList.put("B",true);
////        registerList.put("MR",true);
////        registerList.put("LR",true);
////        registerList.put("CR",true);
////        registerList.put("VB",true);
//
//        registerType_MaxRange.put("DM",65534);
//        registerType_MaxRange.put("EM",65534);
//        registerType_MaxRange.put("FM",32767);
//
//        if(StringUtils.equalsAnyIgnoreCase(cpu,"KV-3000")
//                ||StringUtils.equalsAnyIgnoreCase(cpu,"KV-5000")
//                ||StringUtils.equalsAnyIgnoreCase(cpu,"KV-5500")){
//            registerType_MaxRange.put("ZF",131071);
//            registerType_MaxRange.put("W",16383);
//        }else{
//            registerType_MaxRange.put("W",32767);
//            registerType_MaxRange.put("ZF",524287);
//        }
//        registerType_MaxRange.put("TM",511);
////        registerList.put("Z",true);
////        registerList.put("T",true);
////        registerList.put("TC",true);
////        registerList.put("TS",true);
////        registerList.put("C",true);
////        registerList.put("CC",true);
////        registerList.put("CS",true);
////        registerList.put("AT",true);
////        registerList.put("CM",true);
////        registerList.put("VM",true);
//    }
//
//    /**
//     * .U ：16 位无符号十进制数
//     * .S ：16 位有符号十进制数
//     * .D ：32 位无符号十进制数
//     * .L ：32 位有符号十进制数
//     * .H ：16 位十六进制数
//     */
//    private void initDataType(){
//        dataType.put(DataTypeNames.Uint16,".U");
//        dataType.put(DataTypeNames.int16,".S");
//        dataType.put(DataTypeNames.Uint32,".D");
//        dataType.put(DataTypeNames.int32,".L");
//        dataType.put(DataTypeNames.hex,".H");
//    }
//
////    public static byte[] GetReadCommand(DataAddress address)
////    {
////        StringBuilder stringBuilder = new StringBuilder();
////        stringBuilder.Append("RDS " + address.get_Value());
////        switch (address.get_Type())
////        {
////            case 1:
////                stringBuilder.Append(string.Format(" {0}", 1));
////                break;
////            case 2:
////                stringBuilder.Append(string.Format(".S {0}", address.get_Offset() + 1));
////                break;
////            case 3:
////                stringBuilder.Append(string.Format(".U {0}", address.get_Offset() + 1));
////                break;
////            case 4:
////                stringBuilder.Append(string.Format(".L {0}", address.get_Offset() + 1));
////                break;
////            case 5:
////                stringBuilder.Append(string.Format(".H {0}", (address.get_Offset() + 1) * 2));
////                break;
////            case 6:
////                stringBuilder.Append(string.Format(".H {0}", (int)Math.Ceiling((double)(address.get_Offset() + 1) / 2.0)));
////                break;
////            default:
////                throw new NotImplementedException();
////        }
////        stringBuilder.Append("\r");
////        return Encoding.ASCII.GetBytes(stringBuilder.ToString());
////    }
//
//}
//
