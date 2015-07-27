package org.motechproject.nms.tracking.utils;

import java.util.Collection;

public class Change {

    private Object oldValue;
    private Object newValue;

    public Change(Object oldValue, Object newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    public static boolean isActualChange(Collection<Change> changes) {
        return !changes.isEmpty();
    }
}
