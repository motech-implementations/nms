package org.motechproject.nms.tracking.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class CollectionFactory {

    public abstract Collection<Object> createCollection();

    public static CollectionFactory of(final Class<? extends Collection> clazz) {
        return new CollectionFactory() {
            @Override
            public Collection<Object> createCollection() {
                if (List.class.equals(clazz)) {
                    return new ArrayList<>();
                } else if (Set.class.equals(clazz)) {
                    return new HashSet<>();
                } else {
                    throw new IllegalArgumentException("Cannot instantiate collection of type " + clazz.getName());
                }
            }
        };

    }
}
