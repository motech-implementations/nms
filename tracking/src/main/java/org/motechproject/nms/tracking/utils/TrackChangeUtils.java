package org.motechproject.nms.tracking.utils;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class TrackChangeUtils {

    public static final Logger LOGGER = LoggerFactory.getLogger(TrackChangeUtils.class);

    private TrackChangeUtils() {
    }

    public static void trackChange(TrackChanges target, String property, Object oldValue, Object newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            if (oldValue instanceof Collection || newValue instanceof Collection) { // include null-check
                trackCollectionPropertyChange(target, property, (Collection) oldValue, (Collection) newValue);
            } else {
                trackPropertyChange(target, property, oldValue, newValue);
            }
        }
    }

    public static Collection decorateTrackedCollection(TrackChanges target, String property, Collection collection) {
        if (collection != null) {
            CollectionChange collectionChange = getCollectionChange(target, property);
            if (collection instanceof List) {
                return collection;// new TrackedListDecorator((List) collection, collectionChange);
            } else if (collection instanceof Set) {
                return collection; //new TrackedSetDecorator((Set) collection, collectionChange);
            } else {
                LOGGER.error("Cannot find suitable decorator for collection of type {} ({}.{})",
                        collection.getClass().getName(), target.getClass().getName(), property);
                return collection;
            }
        } else {
            return null;
        }
    }

    private static void trackCollectionPropertyChange(TrackChanges target, String property, Collection oldCollection, Collection newCollection) {
        CollectionChange collectionChange = getCollectionChange(target, property);
        if (oldCollection != null && newCollection != null) {
            Collection intersection = CollectionUtils.intersection(oldCollection, newCollection);
            collectionChange.removed(CollectionUtils.subtract(oldCollection, intersection));
            collectionChange.added(CollectionUtils.subtract(newCollection, intersection));
        } else if (oldCollection != null) {
            collectionChange.removed(oldCollection);
        } else if (newCollection != null) {
            collectionChange.added(newCollection);
        }
    }

    private static CollectionChange getCollectionChange(TrackChanges target, String property) {
        CollectionChange collectionChange = target.collectionChanges().get(property);
        if (collectionChange == null) {
            CollectionFactory collectionFactory = CollectionFactory.of(getCollectionType(target, property));
            collectionChange = new CollectionChange(collectionFactory);
            target.collectionChanges().put(property, collectionChange);
        }
        return collectionChange;
    }

    private static Class<? extends Collection> getCollectionType(TrackChanges target, String property) {
        try {
            return (Class<? extends Collection>) PropertyUtils.getPropertyType(target, property);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("Cannot retrieve property type of %s.%s", target.getClass(), property), e);
        }
    }

    private static void trackPropertyChange(TrackChanges target, String property, Object oldValue, Object newValue) {
        Change change = target.changes().get(property);
        if (change == null) {
            createPropertyChange(target, property, oldValue, newValue);
        } else {
            updatePropertyChange(target, property, newValue, change);
        }
    }

    private static void createPropertyChange(TrackChanges target, String property, Object oldValue, Object newValue) {
        target.changes().put(property, new Change(oldValue, newValue));
    }

    private static void updatePropertyChange(TrackChanges target, String property, Object newValue, Change change) {
        if (!Objects.equals(change.getOldValue(), newValue)) {
            change.setNewValue(newValue);
        } else {
            target.changes().remove(property);
        }
    }
}
