package org.motechproject.nms.csv.utils;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.util.CsvContext;

public class GetLocalDate implements CellProcessor {

    @Override
    public Object execute(Object value, CsvContext context) {
        LocalDate returnValue;
        if (value instanceof LocalDate) {
            returnValue = (LocalDate) value;
        } else if (value instanceof String) {
            try {
                returnValue = "".equals(((String) value).trim()) ? null : LocalDate.parse(value.toString(), DateTimeFormat.forPattern("dd-MM-yyyy"));
            } catch (IllegalArgumentException e) {
                throw new CsvImportDataException(getErrorMessage(value, context), e);
            }
        } else {
            throw new CsvImportDataException(getErrorMessage(value, context));
        }
        return returnValue;
    }

    private String getErrorMessage(Object value, CsvContext context) {
        return String.format("CSV field error [row: %d, col: %d]: Expected LocalDate value, found %s",
                context.getRowNumber(), context.getColumnNumber(), value);
    }

}
