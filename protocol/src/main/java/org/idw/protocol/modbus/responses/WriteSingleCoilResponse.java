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

package org.idw.protocol.modbus.responses;
import org.idw.protocol.modbus.FunctionCode;

/**
 * The normal response is an echo of the request, returned after the coil state has been written.
 */
public class WriteSingleCoilResponse extends SimpleModbusResponse {

    private final int address;
    private final int value;

    /**
     * @param address 0x0000 to 0xFFFF (0 to 65535)
     * @param value   true or false (0xFF00 or 0x0000)
     */
    public WriteSingleCoilResponse(int address, int value) {
        super(FunctionCode.WriteSingleCoil);

        this.address = address;
        this.value = value;
    }

    public int getAddress() {
        return address;
    }

    public int getValue() {
        return value;
    }

}
