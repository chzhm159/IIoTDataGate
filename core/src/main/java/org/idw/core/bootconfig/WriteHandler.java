package org.idw.core.bootconfig;

import org.idw.core.model.Tag;
import org.idw.core.model.TagData4Write;

public interface WriteHandler {
    void doWrite(Tag tag, TagData4Write data);
}
