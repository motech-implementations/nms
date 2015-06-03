package org.motechproject.nms.csv.utils;

import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class CsvMapImporter extends CsvImporter<ICsvMapReader> {

    public Map<String, Object> read() throws IOException {
        if (isOpen()) {
            return getCsvReader().read(getFieldNames(), getProcessors());
        } else {
            throw new IllegalStateException("CsvImporter is closed");
        }
    }

    @Override
    protected ICsvMapReader createCsvReader(Reader reader, CsvPreference preference) {
        if (preference == null) {
            return new CsvMapReader(reader, CsvPreference.STANDARD_PREFERENCE);
        }
        return new CsvMapReader(reader, preference);
    }
}
