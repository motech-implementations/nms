package org.motechproject.nms.csv.utils;

import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.util.CsvContext;

public class GetString implements CellProcessor {

    @Override
    public Object execute(Object value, CsvContext context) {
        if (value instanceof String) {
            if (("NULL").equalsIgnoreCase(String.valueOf(value))) {
                return null;
            }
            return value;
        }

        if (value == null) {
            throw new CsvImportDataException(getErrorMessage(context));
        }

        return String.valueOf(value);
    }

    private String getErrorMessage(CsvContext context) {
        return String.format("CSV field error [row: %d, col: %d]: Expected String value, found null",
                context.getRowNumber(), context.getColumnNumber());
    }
}
