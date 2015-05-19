package org.motechproject.nms.region.utils;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.util.CsvContext;

public class GetString implements CellProcessor {

    @Override
    public Object execute(Object value, CsvContext context) {
        return null == value ? null : String.valueOf(value);
    }
}
