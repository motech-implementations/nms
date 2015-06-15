package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.State;

public interface TalukaImportService extends LocationDataImportService {
    void addParent(State state);
}
