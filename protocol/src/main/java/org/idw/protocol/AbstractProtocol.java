package org.idw.protocol;

import java.util.HashMap;

public abstract class AbstractProtocol {
    protected String cpuModel;
    protected String protocol;
    public abstract Byte[] getReadCommand(HashMap<String,Object> args);
    public abstract Byte[] getWriteCommand(HashMap<String,Object> args);
}
