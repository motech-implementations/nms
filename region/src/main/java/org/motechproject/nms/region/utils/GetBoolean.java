package org.motechproject.nms.region.utils;

import org.motechproject.nms.region.exception.CsvImportDataException;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.util.CsvContext;

public class GetBoolean implements CellProcessor {

    @Override
    public Object execute(Object value, CsvContext context) {
        Boolean returnValue;
        if (value instanceof Boolean) {
            returnValue = (Boolean) value;
        } else if (value instanceof String) {
            returnValue = Boolean.valueOf((String) value);
        } else {
            throw new CsvImportDataException(getErrorMessage(value, context));
        }
        return returnValue;
    }

    private String getErrorMessage(Object value, CsvContext context) {
        return String.format("CSV field error [row: %d, col: %d]: Expected Boolean value, found %s",
                context.getRowNumber(), context.getColumnNumber(), value);
    }
}
