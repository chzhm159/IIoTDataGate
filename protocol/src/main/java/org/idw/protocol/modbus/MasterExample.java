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

package org.idw.protocol.modbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class MasterExample {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public static void main(String[] args) {
        // 原测试
        // new MasterExample(100, 100).start();
        MasterExample me = new MasterExample();
        me.readTest();

    }
    public void readTest(){
        ModbusMasterTCP mmtcp = new ModbusMasterTCP();
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("registerType","ReadHoldingRegisters");
        args.put("registerIndex","12");
        args.put("count","5");
        args.put("transactionId","5");
        args.put("unitId","126");
        args.put("unit","unint16");
        mmtcp.read(args);
    }
    public MasterExample(){

    }


// ===============================================================================================================//
    /*private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final List<ModbusTcpMaster> masters = new CopyOnWriteArrayList<>();
    private volatile boolean started = false;

    private final int nMasters;
    private final int nRequests;

    public MasterExample(int nMasters, int nRequests) {
        this.nMasters = nMasters;
        this.nRequests = nRequests;
    }

    public void start() {
        started = true;

        ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder("localhost")
            .setPort(50200)
            .build();

        new Thread(() -> {
            while (started) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                double mean = 0.0;
                double oneMinute = 0.0;

                for (ModbusTcpMaster master : masters) {
                    mean += master.getResponseTimer().getMeanRate();
                    oneMinute += master.getResponseTimer().getOneMinuteRate();
                }

                logger.info("Mean rate={}, 1m rate={}", mean, oneMinute);
            }
        }).start();

        for (int i = 0; i < nMasters; i++) {
            ModbusTcpMaster master = new ModbusTcpMaster(config);
            master.connect();

            masters.add(master);

            for (int j = 0; j < nRequests; j++) {
                sendAndReceive(master);
            }
        }
    }

    private void sendAndReceive(ModbusTcpMaster master) {
        if (!started) return;

        CompletableFuture<ReadHoldingRegistersResponse> future =
            master.sendRequest(new ReadHoldingRegistersRequest(0, 10), 0);

        future.whenCompleteAsync((response, ex) -> {
            if (response != null) {
                // ReferenceCountUtil.release(response);
            } else {
                logger.error("Completed exceptionally, message={}", ex.getMessage(), ex);
            }
            scheduler.schedule(() -> sendAndReceive(master), 1, TimeUnit.SECONDS);
        }, Modbus.sharedExecutor());
    }

    public void stop() {
        started = false;
        masters.forEach(ModbusTcpMaster::disconnect);
        masters.clear();
    }
*/
}
