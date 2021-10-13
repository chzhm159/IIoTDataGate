package org.idw.protocol.modbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class TransactionId {
    private static final Logger log = LoggerFactory.getLogger(TransactionId.class);
    private static int MAX = 65535;
    private static ArrayList<Boolean> cache = new ArrayList<Boolean>(MAX);
    private static AtomicInteger currentId = new AtomicInteger(-1);

    private static TransactionId inst = null;
    private TransactionId(){
        for(int i=0;i<=MAX;i++){
            cache.add(i,false);
        }
    }
    public static TransactionId getInst(){
        if(inst==null){
            inst = new TransactionId();
            return inst;
        }
        return inst;
    }

    /**
     * 获取一个 Modbus tcp 所使用的 会话ID,其值的范围为 0~65535.默认为顺序递增返回
     * @return 事务编号
     */
    public int getTransactionId() {
        int currentIdx = currentId.get();
        if(currentIdx==MAX){
            log.error("The transaction id value was empty");
            return -1;
        }
        int id = currentId.addAndGet(1);
        boolean  used = cache.get(id);
        if (!used){
            cache.add(id,true);
            return id;
        }else{
            return getTransactionId();
        }
    }

//    public static void main(String[] args){
//        TransactionId tid = TransactionId.getInst();
//        for(int i=0;i<=65540;i++){
//            log.debug("事务id:{}",tid.getTransactionId());
//        }
//    }
}
