package org.motechproject.nms.tracking.utils;

import com.google.common.collect.ForwardingList;

import java.util.Collection;
import java.util.List;

public class TrackedListDecorator<T> extends ForwardingList<T> {

    private final List<T> list;
    private final CollectionChange change;
    private final TrackedCollectionHelper<T> listHelper;

    public TrackedListDecorator(List<T> list, CollectionChange change) {
        this.list = list;
        this.change = change;
        this.listHelper = new TrackedCollectionHelper<>(list, change);
    }

    @Override
    public boolean add(T t) {
        return listHelper.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return listHelper.remove(o);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return listHelper.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return listHelper.removeAll(c);
    }

    @Override
    public void clear() {
        listHelper.clear();
    }

    @Override
    public T set(int index, T element) {
        T removed = list.set(index, element);
        change.removed(removed);
        change.added(element);
        return removed;
    }

    @Override
    public void add(int index, T element) {
        list.add(index, element);
        change.added(element);
    }

    @Override
    public T remove(int index) {
        T removed = list.remove(index);
        change.removed(removed);
        return removed;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return listHelper.retainAll(c);
    }

    @Override
    protected List<T> delegate() {
        return list;
    }
}
