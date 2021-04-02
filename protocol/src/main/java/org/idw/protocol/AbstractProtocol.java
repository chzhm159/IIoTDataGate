package org.idw.protocol;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class AbstractProtocol {
    protected String cpuModel;
    protected String protocol;
    public abstract ArrayList<Byte> getReadCommand(HashMap<String,Object> args);
    public abstract  ArrayList<Byte> getWriteCommand(HashMap<String,Object> args);
}
