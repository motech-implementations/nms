package org.motechproject.nms.region.utils;

import org.supercsv.util.CsvContext;

public abstract class GetInstanceByString<T> extends GetString {

    @Override
    public Object execute(Object value, CsvContext context) {
        String stringValue = (String) super.execute(value, context);
        return retrieve(stringValue);
    }

    public abstract T retrieve(String value);

}
