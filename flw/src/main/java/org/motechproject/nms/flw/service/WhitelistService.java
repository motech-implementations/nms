package org.motechproject.nms.flw.service;

import org.motechproject.nms.location.domain.State;

public interface WhitelistService {
    boolean numberWhitelistedForState(State state, String contactNumber);
}
