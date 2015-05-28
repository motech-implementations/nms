package org.motechproject.nms.region.utils;

import org.motechproject.nms.region.exception.CsvImportDataException;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.util.CsvContext;

public class GetInteger implements CellProcessor {

    @Override
    public Object execute(Object value, CsvContext context) {
        Integer returnValue;
        if (value instanceof Number) {
            returnValue = ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                returnValue = Integer.valueOf((String) value);
            } catch (NumberFormatException e) {
                throw new CsvImportDataException(getErrorMessage(value, context), e);
            }
        } else {
            throw new CsvImportDataException(getErrorMessage(value, context));
        }
        return returnValue;
    }

    private String getErrorMessage(Object value, CsvContext context) {
        return String.format("CSV field error [row: %d, col: %d]: Expected Integer value, found %s",
                context.getRowNumber(), context.getColumnNumber(), value);
    }

}
