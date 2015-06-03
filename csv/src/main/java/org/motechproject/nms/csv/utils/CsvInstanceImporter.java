package org.motechproject.nms.csv.utils;

import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.Reader;

public class CsvInstanceImporter<T> extends CsvImporter<ICsvBeanReader> {

    private Class<T> type;

    public CsvInstanceImporter(Class<T> type) {
        this.type = type;
    }

    public T read() throws IOException {
        if (isOpen()) {
            return getCsvReader().read(type, getFieldNames(), getProcessors());
        } else {
            throw new IllegalStateException("CsvImporter is closed");
        }
    }

    @Override
    protected ICsvBeanReader createCsvReader(Reader reader, CsvPreference preference) {
        if (preference == null) {
            return new CsvBeanReader(reader, CsvPreference.STANDARD_PREFERENCE);
        }
        return new CsvBeanReader(reader, preference);
    }
}
