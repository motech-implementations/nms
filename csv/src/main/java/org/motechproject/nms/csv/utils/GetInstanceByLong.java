package org.motechproject.nms.csv.utils;

import org.supercsv.util.CsvContext;

public abstract class GetInstanceByLong<T> extends GetLong {

    @Override
    public Object execute(Object value, CsvContext context) {
        Long longValue = (Long) super.execute(value, context);
        return retrieve(longValue);
    }

    public abstract T retrieve(Long value);

}
