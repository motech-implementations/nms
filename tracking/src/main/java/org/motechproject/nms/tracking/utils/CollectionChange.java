package org.motechproject.nms.tracking.utils;

import java.util.Collection;

public class CollectionChange {

    private Collection<Object> added;
    private Collection<Object> removed;

    public CollectionChange(CollectionFactory collectionFactory) {
        this.added = collectionFactory.createCollection();
        this.removed = collectionFactory.createCollection();
    }

    public void added(Object object) {
        added.add(object);
    }

    public void added(Collection<Object> collection) {
        for (Object object : collection) {
            added(object);
        }
    }

    public void removed(Object object) {
        removed.add(object);
    }

    public void removed(Collection<Object> collection) {
        for (Object object : collection) {
            removed(object);
        }
    }

    public Collection<Object> getAdded() {
        return added;
    }

    public Collection<Object> getRemoved() {
        return removed;
    }

    public boolean isActualCollectionChange() {
        return !getAdded().isEmpty() || !getRemoved().isEmpty();
    }

    public static boolean isActualCollectionChange(Collection<CollectionChange> collectionChanges) {
        if (collectionChanges.isEmpty()) {
            return false;
        } else {
            for (CollectionChange collectionChange : collectionChanges) {
                if (collectionChange.isActualCollectionChange()) {
                    return true;
                }
            }
            return false;
        }
    }
}
