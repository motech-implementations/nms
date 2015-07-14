package org.motechproject.nms.tracking.service.impl;

import org.joda.time.DateTime;
import org.motechproject.mds.domain.InstanceLifecycleListenerType;
import org.motechproject.mds.listener.MotechLifecycleListener;
import org.motechproject.mds.service.EntityService;
import org.motechproject.mds.service.JdoListenerRegistryService;
import org.motechproject.mds.util.Constants;
import org.motechproject.mds.util.PropertyUtil;
import org.motechproject.nms.tracking.domain.ChangeLog;
import org.motechproject.nms.tracking.exception.TrackChangesException;
import org.motechproject.nms.tracking.repository.ChangeLogDataService;
import org.motechproject.nms.tracking.service.TrackChangesService;
import org.motechproject.nms.tracking.utils.Change;
import org.motechproject.nms.tracking.utils.TrackChanges;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TrackChangesServiceImpl implements TrackChangesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackChangesServiceImpl.class);
    public static final String PRE_STORE = "preStore";
    public static final String PRE_DELETE = "preDelete";
    public static final String EMPTY_PACKAGE = "";

    private JdoListenerRegistryService jdoListenerRegistryService;
    private EntityService entityService;
    private ChangeLogDataService changeLogDataService;

    private Set<String> trackedClasses = new HashSet<>();

    @Override
    public void registerLifecycleListeners(Class<?> clazz) {
        if (TrackChanges.class.isAssignableFrom(clazz)) {
            String className = clazz.getName();
            if (!trackedClasses.contains(className)) {
                MotechLifecycleListener preStoreListener = createListener(className, PRE_STORE, InstanceLifecycleListenerType.PRE_STORE);
                MotechLifecycleListener preDeleteListener = createListener(className, PRE_DELETE, InstanceLifecycleListenerType.PRE_DELETE);
                jdoListenerRegistryService.registerListener(preStoreListener);
                jdoListenerRegistryService.registerListener(preDeleteListener);
                trackedClasses.add(className);
            }
        } else {
            LOGGER.warn("Tracked class should implement the {} interface", TrackChanges.class.getName());
        }
    }

    @Override
    public void preStore(Object target) {
        if (target instanceof TrackChanges && isEntityInstance(target)) {
            try {
                storeChangeLog(target);
            } catch (TrackChangesException e) {
                LOGGER.error("Unable to store change log", e);
            }
        } else {
            LOGGER.error("Unable to track changes of " + target.getClass());
        }
    }

    @Override
    public void preDelete(Object target) {
        if (target instanceof TrackChanges && isEntityInstance(target)) {
            try {
                deleteChangeLogs(target);
            } catch (TrackChangesException e) {
                LOGGER.error("Unable to delete change logs");
            }
        } else {
            LOGGER.error("Unable to track changes of " + target.getClass());
        }
    }

    private void storeChangeLog(Object target) throws TrackChangesException {
        Map<String, Change> changes = ((TrackChanges) target).changes();
        if (!changes.isEmpty()) {
            String entityName = getEntityName(target);
            Long instanceId = getInstanceId(target);
            String change = getChange(changes);
            ChangeLog changeLog = new ChangeLog(entityName, instanceId, DateTime.now(), change);
            changeLogDataService.create(changeLog);
        }
    }

    private void deleteChangeLogs(Object target) throws TrackChangesException {
        String entityName = getEntityName(target);
        Long instanceId = getInstanceId(target);
        List<ChangeLog> changeLogs = changeLogDataService.findByEntityNameAndInstanceId(entityName, instanceId);
        for (ChangeLog changeLog : changeLogs) {
            changeLogDataService.delete(changeLog);
        }
    }

    private String getEntityName(Object target) {
        return target.getClass().getName();
    }

    private Long getInstanceId(Object target) throws TrackChangesException {
        try {
            return (Long) PropertyUtil.getProperty(target, Constants.Util.ID_FIELD_NAME);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassCastException e) {
            throw new TrackChangesException("Unable to retrieve instance id", e);
        }
    }

    private String getChange(Map<String, Change> changes) throws TrackChangesException {
        StringBuilder builder = new StringBuilder();
        for (Iterator<Map.Entry<String, Change>> iterator = changes.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Change> changeEntry = iterator.next();
            String propertyName = changeEntry.getKey();
            Change change = changeEntry.getValue();
            builder.append(propertyName)
                    .append("(")
                    .append(formatPropertyValue(change.getOldValue()))
                    .append(", ")
                    .append(formatPropertyValue(change.getNewValue()))
                    .append(")");
            if (iterator.hasNext()) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    private String formatPropertyValue(Object value) throws TrackChangesException {
        if (isEntityInstance(value)) {
            return String.valueOf(getInstanceId(value));
        } else {
            return String.valueOf(value);
        }
    }

    private boolean isEntityInstance(Object object) {
        return object != null && entityService.getEntityByClassName(object.getClass().getName()) != null;
    }

    private MotechLifecycleListener createListener(String className, String serviceMethod, InstanceLifecycleListenerType listenerType) {
        return new MotechLifecycleListener(TrackChangesServiceImpl.class, serviceMethod, className,
                EMPTY_PACKAGE, new InstanceLifecycleListenerType[] {listenerType}, Collections.singletonList(className));
    }

    @Autowired
    public void setJdoListenerRegistryService(JdoListenerRegistryService jdoListenerRegistryService) {
        this.jdoListenerRegistryService = jdoListenerRegistryService;
    }

    @Autowired
    public void setEntityService(EntityService entityService) {
        this.entityService = entityService;
    }

    @Autowired
    public void setChangeLogDataService(ChangeLogDataService changeLogDataService) {
        this.changeLogDataService = changeLogDataService;
    }
}
