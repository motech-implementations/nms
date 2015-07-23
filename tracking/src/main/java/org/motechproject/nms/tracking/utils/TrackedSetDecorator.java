package org.motechproject.nms.tracking.utils;

import com.google.common.collect.ForwardingSet;

import java.util.Collection;
import java.util.Set;

public class TrackedSetDecorator<T> extends ForwardingSet<T> {

    private final Set<T> set;
    private final TrackedCollectionHelper<T> setHelper;

    public TrackedSetDecorator(Set<T> set, CollectionChange change) {
        this.set = set;
        this.setHelper = new TrackedCollectionHelper<>(set, change);
    }

    @Override
    public boolean add(T t) {
        return setHelper.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return setHelper.remove(o);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return setHelper.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return setHelper.removeAll(c);
    }

    @Override
    public void clear() {
        setHelper.clear();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return setHelper.retainAll(c);
    }

    @Override
    protected Set<T> delegate() {
        return set;
    }
}
