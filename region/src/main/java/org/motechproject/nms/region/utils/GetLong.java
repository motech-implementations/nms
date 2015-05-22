package org.motechproject.nms.region.utils;

import org.motechproject.nms.region.exception.CsvImportDataException;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.util.CsvContext;

public class GetLong implements CellProcessor {

    @Override
    public Object execute(Object value, CsvContext context) {
        Long returnValue;
        if (value instanceof Number) {
            returnValue = ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                returnValue = Long.valueOf((String) value);
            } catch (NumberFormatException e) {
                throw new CsvImportDataException(getErrorMessage(value, context), e);
            }
        } else {
            throw new CsvImportDataException(getErrorMessage(value, context));
        }
        return returnValue;
    }

    private String getErrorMessage(Object value, CsvContext context) {
        return String.format("CSV field error [row: %d, col: %d]: Expected Long value, found %s",
                context.getRowNumber(), context.getColumnNumber(), value);
    }

}
