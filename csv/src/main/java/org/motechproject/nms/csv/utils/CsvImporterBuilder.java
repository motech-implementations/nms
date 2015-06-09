package org.motechproject.nms.csv.utils;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class CsvImporterBuilder {

    private Map<String, String> fieldNameMapping;
    private Map<String, CellProcessor> processorMapping;
    private CsvPreference preferences = CsvPreference.STANDARD_PREFERENCE;

    public CsvImporterBuilder setFieldNameMapping(Map<String, String> fieldNameMapping) {
        this.fieldNameMapping = fieldNameMapping;
        return this;
    }

    public CsvImporterBuilder setProcessorMapping(Map<String, CellProcessor> processorMapping) {
        this.processorMapping = processorMapping;
        return this;
    }

    public CsvImporterBuilder setPreferences(CsvPreference preferences) {
        this.preferences = preferences;
        return this;
    }

    public CsvMapImporter createAndOpen(Reader reader) throws IOException {
        CsvMapImporter importer = new CsvMapImporter();
        importer.open(reader, preferences, processorMapping, fieldNameMapping);
        return importer;
    }

    public <T> CsvInstanceImporter<T> createAndOpen(Reader reader, Class<T> instanceType) throws IOException {
        CsvInstanceImporter<T> importer = new CsvInstanceImporter<>(instanceType);
        importer.open(reader, preferences, processorMapping, fieldNameMapping);
        return importer;
    }
}
