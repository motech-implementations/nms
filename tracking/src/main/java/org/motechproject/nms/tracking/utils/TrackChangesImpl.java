package org.motechproject.nms.tracking.utils;

import java.util.HashMap;
import java.util.Map;

public class TrackChangesImpl implements TrackChanges {

    private Map<String, Change> changes;

    @Override
    public Map<String, Change> changes() {
        if (null == changes) {
            changes = new HashMap<>();
        }
        return changes;
    }
}
