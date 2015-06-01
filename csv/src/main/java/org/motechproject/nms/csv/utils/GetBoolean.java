package org.motechproject.nms.csv.utils;

import org.apache.commons.lang.StringUtils;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.util.CsvContext;

public class GetBoolean implements CellProcessor {

    @Override
    public Object execute(Object value, CsvContext context) {
        Boolean returnValue;
        if (value instanceof Boolean) {
            returnValue = (Boolean) value;
        } else if (value instanceof String) {
            String string = (String) value;
            if (equalsAnyIgnoreCase(string, "true", "t", "yes", "y")) {
                returnValue = true;
            } else if (equalsAnyIgnoreCase(string, "false", "f", "no", "n")) {
                returnValue = false;
            } else {
                throw new CsvImportDataException(getErrorMessage(value, context));
            }
        } else {
            throw new CsvImportDataException(getErrorMessage(value, context));
        }
        return returnValue;
    }

    private String getErrorMessage(Object value, CsvContext context) {
        return String.format("CSV field error [row: %d, col: %d]: Expected Boolean value, found %s",
                context.getRowNumber(), context.getColumnNumber(), value);
    }

    private boolean equalsAnyIgnoreCase(String string, String... equalsStrings) {
        for (String equalString : equalsStrings) {
            if (StringUtils.equalsIgnoreCase(string, equalString)) {
                return true;
            }
        }
        return false;
    }
}
