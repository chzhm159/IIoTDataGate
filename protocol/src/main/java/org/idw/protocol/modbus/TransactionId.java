package org.idw.protocol.modbus;

import com.google.common.cache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class TransactionId {
    private static final Logger log = LoggerFactory.getLogger(TransactionId.class);
    private int MIN = 0;
    private int MAX = 65535;
    private long EXPIRE=1000;
    private ArrayList<SID> sidPool = new ArrayList<SID>(MAX);
    private AtomicInteger incrementIdx = new AtomicInteger(-1);

    private static TransactionId inst = null;
    private TransactionId(int min,int max,long expire){
        this.MIN=min;
        this.MAX=max;
        this.EXPIRE=expire;
        for(int i=MIN;i<=MAX;i++){
            sidPool.add(i,new SID(i,Long.MAX_VALUE,expire));
        }
    }

    /**
     * 获取一个 会话ID管理对象
     * @param min sid 初始数值
     * @param max sid 最大数值
     * @param expire 自获取开始计时,超过指定毫秒数后会被回收再利用
     * @return 返回一个可用的 sid
     */
    public static TransactionId getInst(int min,int max,long expire){
        if(inst==null){
            inst = new TransactionId(min,max,expire);
            return inst;
        }
        return inst;
    }

    /**
     * 获取一个 会话ID,默认为顺序递增返回,若达到最大数值后从头开始循环产生
     * @return 事务编号
     */
    public int getSID(long expire) {
        int icIdx = incrementIdx.get();
        if(icIdx==MAX){
            // 虽然不够严谨,但只要不做非常严密测试基本可用
            log.warn("The transaction id value was empty");
            incrementIdx.compareAndSet(MAX,-1);
        }
        int key = incrementIdx.addAndGet(1);
        SID sid = sidPool.get(key);
        Calendar c = Calendar.getInstance();
        long ts = c.getTimeInMillis();
        boolean exp = ((ts- sid.timestamp )>sid.expire);
        // log.debug("事务id:{},是否被占用:{},是否过期={}",key,sid.used,exp);
        // 未被使用,或者已经过期则可以直接使用
        if (!sid.used || exp) {
            sid.timestamp=ts;
            sid.expire=expire;
            sid.used=true;
            sidPool.set(key, sid);
            return key;
        } else{
            // 如果 sid 已经占用到MAX,0号sid仍然没有被回收,则只能从1号开始往后遍历至最大可用数值,寻找第一个未被使用的sid
            int reusedSID=-1;
            // id代表的数值已经被验证过了,所以直接跳过
            for(int a=key+1; a<=MAX;a++){
                SID resid = sidPool.get(a);
                boolean reExp = ((ts- resid.timestamp )>resid.expire);
                if(!resid.used || reExp){
                    log.debug("SID reused [{}]",a);
                    incrementIdx.set(a);
                    resid.timestamp=ts;
                    resid.used=true;
                    resid.expire=expire;
                    sidPool.set(a,resid);
                    reusedSID = a;
                    break;
                }
            }
            return reusedSID;
        }
    }
    public void releaseSID(int txid){
        if(txid<MIN || txid>MAX){return;}
        //log.debug("invalidate sid [{}]",txid);
        SID resid = sidPool.get(txid);
        resid.timestamp=Long.MAX_VALUE;
        resid.used=false;
        resid.expire=this.EXPIRE;
        sidPool.set(txid,resid);
    }

    public static void main(String[] args){
        TransactionId tid = TransactionId.getInst(0,16,10);

        Thread get = new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<=160;i++){
                    int sid = tid.getSID(1000);
                   // log.debug("可用 sid------------------> {}",sid);
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Thread releasd = new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<=160;i++){
                    tid.releaseSID(i%16);
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        get.start();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        releasd.start();

    }
    private class SID{
        public int value;
        public long timestamp;
        public long expire;
        public boolean used=false;
        public SID(int v,long timestamp,long expire){
            this.value=v;
            this.timestamp=timestamp;
            this.expire=expire;
        }
    }
}
