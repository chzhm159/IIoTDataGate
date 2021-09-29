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


import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ModbusTcpCodec {
    // mbap包头长度
    private static final int HeaderLength = MbapHeader.LENGTH;
    //
    private static final int HeaderSize = 6;
    private static final int LengthFieldIndex = 4;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ModbusPduEncoder encoder;
    private ModbusPduDecoder decoder;
    public ModbusTcpCodec(ModbusPduEncoder encoder,ModbusPduDecoder decoder){
        this.encoder = encoder;
        this.decoder = decoder;
    }

    protected void encode(ModbusTcpPayload payload, ByteBuf buffer) {
        int headerStartIndex = buffer.writerIndex();
        buffer.writeZero(MbapHeader.LENGTH);

        int pduStartIndex = buffer.writerIndex();
        encoder.encode(payload.getModbusPdu(), buffer);
        int pduLength = buffer.writerIndex() - pduStartIndex;

        MbapHeader header = new MbapHeader(
            payload.getTransactionId(),
            pduLength + 1,
            payload.getUnitId()
        );

        int currentWriterIndex = buffer.writerIndex();
        buffer.writerIndex(headerStartIndex);
        MbapHeader.encode(header, buffer);
        buffer.writerIndex(currentWriterIndex);
    }

    protected void decode(ByteBuf buffer, List<Object> out) throws Exception {
        int startIndex = buffer.readerIndex();

        while (buffer.readableBytes() >= HeaderLength &&
            buffer.readableBytes() >= getLength(buffer, startIndex) + HeaderSize) {

            try {
                MbapHeader mbapHeader = MbapHeader.decode(buffer);
                ModbusPdu modbusPdu = decoder.decode(buffer);

                if (modbusPdu instanceof UnsupportedPdu) {
                    // Advance past any bytes we should have read but didn't...
                    int endIndex = startIndex + getLength(buffer, startIndex) + 6;
                    buffer.readerIndex(endIndex);
                }

                out.add(new ModbusTcpPayload(mbapHeader.getTransactionId(), mbapHeader.getUnitId(), modbusPdu));
            } catch (Throwable t) {
                throw new Exception("error decoding header/pdu", t);
            }

            startIndex = buffer.readerIndex();
        }
    }

    private int getLength(ByteBuf in, int startIndex) {
        return in.getUnsignedShort(startIndex + LengthFieldIndex);
    }

}
