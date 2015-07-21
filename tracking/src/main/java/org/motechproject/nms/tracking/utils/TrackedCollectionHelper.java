package org.motechproject.nms.tracking.utils;

import java.util.Collection;

public class TrackedCollectionHelper<T> {

    private final Collection<T> collection;
    private final CollectionChange change;

    public TrackedCollectionHelper(Collection<T> collection, CollectionChange change) {
        this.collection = collection;
        this.change = change;
    }

    public boolean add(T t) {
        boolean added = collection.add(t);
        if (added) {
            change.added(t);
        }
        return added;
    }

    public boolean remove(Object o) {
        boolean removed = collection.remove(o);
        if (removed) {
            change.removed(o);
        }
        return removed;
    }

    public boolean addAll(Collection<? extends T> c) {
        boolean added = false;
        for (T t : c) {
            added |= add(t);
        }
        return added;
    }

    public boolean removeAll(Collection<?> c) {
        boolean removed = false;
        for (Object o : c) {
            removed |= remove(o);
        }
        return removed;
    }

    public void clear() {
        for (T t : collection) {
            change.removed(t);
        }
        collection.clear();
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
}
