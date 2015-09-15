package org.motechproject.nms.tracking.service.impl;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.InstanceLifecycleListenerType;
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
import org.motechproject.nms.tracking.utils.CollectionChange;
import org.motechproject.nms.tracking.utils.TrackChanges;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
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
        if (target instanceof TrackChanges && isEntityInstance(target.getClass().getName())) {
            try {
                storeChangeLog((TrackChanges) target);
            } catch (TrackChangesException e) {
                LOGGER.error("Unable to store change log", e);
            }
        } else {
            LOGGER.error("Unable to track changes of " + target.getClass());
        }
    }

    @Override
    public void preDelete(Object target) {
        if (target instanceof TrackChanges && isEntityInstance(target.getClass().getName())) {
            try {
                deleteChangeLogs(target);
            } catch (TrackChangesException e) {
                LOGGER.error("Unable to delete change logs");
            }
        } else {
            LOGGER.error("Unable to track changes of " + target.getClass());
        }
    }

    private void storeChangeLog(TrackChanges target) throws TrackChangesException {
        Map<String, Change> changes = target.changes();
        Map<String, CollectionChange> collectionChanges = target.collectionChanges();
        boolean actualChange = Change.isActualChange(changes.values());
        boolean actualCollectionChange = CollectionChange.isActualCollectionChange(collectionChanges.values());
        if (actualChange || actualCollectionChange) {
            String entityName = getEntityName(target);
            Long instanceId = getInstanceId(target);
            String change = getChange(changes, collectionChanges, actualChange, actualCollectionChange);
            ChangeLog changeLog = new ChangeLog(entityName, instanceId, DateTime.now(), change);
            changeLogDataService.create(changeLog);
            changes.clear();
            collectionChanges.clear();
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

    private String getChange(Map<String, Change> changes, Map<String, CollectionChange> collectionChanges,
                             boolean actualChange, boolean actualCollectionChange) throws TrackChangesException {
        StringBuilder builder = new StringBuilder();
        if (actualChange) {
            buildChanges(changes, builder);
        }
        if (actualChange && actualCollectionChange) {
            builder.append(",");
        }
        if (actualCollectionChange) {
            buildCollectionChanges(collectionChanges, builder);
        }
        return builder.toString();
    }

    private void buildChanges(Map<String, Change> changes, StringBuilder builder) throws TrackChangesException {
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
    }

    private void buildCollectionChanges(Map<String, CollectionChange> collectionChanges, StringBuilder builder) throws TrackChangesException {
        for (Iterator<Map.Entry<String, CollectionChange>> iterator = collectionChanges.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, CollectionChange> collectionChangeEntry = iterator.next();
            buildCollectionChange(builder, collectionChangeEntry);
            if (iterator.hasNext()) {
                builder.append(",");
            }
        }
    }

    private void buildCollectionChange(StringBuilder builder, Map.Entry<String, CollectionChange> collectionChangeEntry) throws TrackChangesException {
        String propertyName = collectionChangeEntry.getKey();
        CollectionChange collectionChange = collectionChangeEntry.getValue();
        builder.append(propertyName).append("(");
        buildCollectionAdded(collectionChange.getAdded(), builder);
        if (!collectionChange.getAdded().isEmpty() && !collectionChange.getRemoved().isEmpty()) {
            builder.append(", ");
        }
        buildCollectionRemoved(collectionChange.getRemoved(), builder);
        builder.append(")");
    }

    private void buildCollectionAdded(Collection<Object> collectionAdded, StringBuilder builder) throws TrackChangesException {
        if (!collectionAdded.isEmpty()) {
            builder.append("added[");
            for (Iterator<Object> iterator = collectionAdded.iterator(); iterator.hasNext(); ) {
                Object added = iterator.next();
                builder.append(formatPropertyValue(added));
                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            builder.append("]");
        }
    }

    private void buildCollectionRemoved(Collection<Object> collectionRemoved, StringBuilder builder) throws TrackChangesException {
        if (!collectionRemoved.isEmpty()) {
            builder.append("removed[");
            for (Iterator<Object> iterator = collectionRemoved.iterator(); iterator.hasNext(); ) {
                Object removed = iterator.next();
                builder.append(formatPropertyValue(removed));
                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            builder.append("]");
        }
    }

    private String formatPropertyValue(Object value) throws TrackChangesException {
        if (value != null && isEntityInstance(value.getClass().getName())) {
            return String.valueOf(getInstanceId(value));
        } else {
            return String.valueOf(value);
        }
    }

    @Cacheable("entityInstance")
    private boolean isEntityInstance(String className) {
        return entityService.getEntityByClassName(className) != null;
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
