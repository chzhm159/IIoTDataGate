package org.idw.protocol.modbus;

import org.idw.protocol.AbstractProtocol;

import java.util.ArrayList;
import java.util.HashMap;

public class ModbusTCP extends AbstractProtocol {
    @Override
    public ArrayList<Byte> getReadCommand(HashMap<String, Object> args) {
        return null;
    }

    @Override
    public ArrayList<Byte> getWriteCommand(HashMap<String, Object> args) {
        return null;
    }
}
