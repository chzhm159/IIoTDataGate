package org.idw.core.bootconfig;

import org.idw.core.model.Tag;
import org.idw.core.model.TagValue;

public interface WriteHandler {
    void doWrite(Tag tag, TagValue data);
}
