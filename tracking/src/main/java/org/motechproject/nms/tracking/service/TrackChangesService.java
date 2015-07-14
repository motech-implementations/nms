package org.motechproject.nms.tracking.service;

public interface TrackChangesService {

    void registerLifecycleListeners(Class<?> entityClass);

    void preStore(Object trackChanges);

    void preDelete(Object trackChanges);
}
