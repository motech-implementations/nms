package org.motechproject.nms.csv.utils;

import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.util.CsvContext;

public class GetString implements CellProcessor {

    @Override
    public Object execute(Object value, CsvContext context) {
        if (value instanceof String) {
            return value;
        } else if (null != value) {
            return String.valueOf(value);
        } else {
            throw new CsvImportDataException(getErrorMessage(context));
        }
    }

    private String getErrorMessage(CsvContext context) {
        return String.format("CSV field error [row: %d, col: %d]: Expected String value, found null",
                context.getRowNumber(), context.getColumnNumber());
    }
}
