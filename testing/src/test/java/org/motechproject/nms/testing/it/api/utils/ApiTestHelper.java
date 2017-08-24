package org.motechproject.nms.testing.it.api.utils;

import org.motechproject.nms.flw.domain.FlwJobStatus;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;

/**
 * Api test helper with static methods
 */
public final class ApiTestHelper {

    public static FrontLineWorker createFlw(String name, Long phoneNumber, String mctsFlwId, FrontLineWorkerStatus status) {
        FrontLineWorker flw = new FrontLineWorker(name, phoneNumber);
        flw.setMctsFlwId(mctsFlwId);
        flw.setStatus(status);
        flw.setJobStatus(FlwJobStatus.ACTIVE);
        return flw;
    }
}
