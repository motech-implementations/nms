package org.motechproject.nms.flw.service;

import org.motechproject.nms.flw.domain.CallContent;

public interface CallContentService {
    void add(CallContent callContent);

    void update(CallContent record);

    void delete(CallContent record);
}
