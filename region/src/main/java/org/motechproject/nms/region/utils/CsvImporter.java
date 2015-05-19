package org.motechproject.nms.region.utils;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvImporter<T> implements Closeable {

    private CsvBeanReader csvReader;
    private Class<T> type;
    private String[] fieldNames;
    private CellProcessor[] processors;

    public CsvImporter(Class<T> type) {
        this.type = type;
    }

    public void open(Reader reader, Map<String, String> fieldNameMapping, Map<String, CellProcessor> processorMapping)
            throws IOException {
        if (null == this.csvReader) {
            this.csvReader = new CsvBeanReader(reader, CsvPreference.STANDARD_PREFERENCE);
            String[] header = csvReader.getHeader(true);
            this.fieldNames = getFieldNames(header, fieldNameMapping);
            this.processors = getProcessors(header, processorMapping);
        } else {
            throw new IllegalStateException("CsvImporter is already open");
        }
    }

    public T read() throws IOException {
        if (null != csvReader) {
            return csvReader.read(type, fieldNames, processors);
        } else {
            throw new IllegalStateException("CsvImporter is closed");
        }
    }

    @Override
    public void close() throws IOException {
        if (null != csvReader) {
            csvReader.close();
            csvReader = null;
            fieldNames = null;
            processors = null;
        }
    }

    public int getRowNumber() {
        if (null != csvReader) {
            return csvReader.getRowNumber();
        } else {
            return -1;
        }
    }

    private CellProcessor[] getProcessors(String[] header, Map<String, CellProcessor> processorMapping) {
        List<CellProcessor> processorsList = new ArrayList<>(header.length);
        for (String column : header) {
            if (processorMapping.containsKey(column)) {
                processorsList.add(processorMapping.get(column));
            } else {
                throw new IllegalStateException(String.format("Cell processor for column '%s' not specified", column));
            }
        }

        return processorsList.toArray(new CellProcessor[processorsList.size()]);
    }

    private String[] getFieldNames(String[] header, Map<String, String> fieldNameMapping) {
        List<String> fieldNamesList = new ArrayList<>(header.length);
        for (String column : header) {
            if (fieldNameMapping.containsKey(column)) {
                fieldNamesList.add(fieldNameMapping.get(column));
            } else {
                throw new IllegalStateException(String.format("Field name for column '%s' not specified", column));
            }
        }

        return fieldNamesList.toArray(new String[fieldNamesList.size()]);
    }
}
