package org.motechproject.nms.csv.utils;

import org.apache.commons.beanutils.BeanUtils;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvInstanceImporter<T> implements Closeable {

    private Class<T> type;
    private ICsvMapReader csvReader;
    private String[] columnNames;
    private Map<String, String> fieldMapping;
    private Map<String, CellProcessor> processorMapping;

    public CsvInstanceImporter(Class<T> type) {
        this.type = type;
    }

    public T read() throws IOException {
        if (isOpen()) {
            T instance = createInstance();
            Map<String, String> cells = csvReader.read(columnNames);
            if (null != cells) {
                Map<String, ?> properties = processCells(cells);
                setInstanceProperties(instance, properties);
                return instance;
            } else {
                return null;
            }
        } else {
            throw new IllegalStateException("CsvImporter is closed");
        }
    }

    public void open(Reader reader, CsvPreference preferences, Map<String, CellProcessor> processorMapping, Map<String, String> fieldNameMapping)
            throws IOException {
        if (null == this.csvReader) {
            this.csvReader = new CsvMapReader(reader, preferences);
            String[] header = csvReader.getHeader(true);
            this.columnNames = getColumnNames(header, fieldNameMapping);
            this.fieldMapping = fieldNameMapping;
            this.processorMapping = processorMapping;
        } else {
            throw new IllegalStateException("CsvImporter is already open");
        }
    }

    @Override
    public void close() throws IOException {
        if (null != csvReader) {
            csvReader.close();
            csvReader = null;
            columnNames = null;
            fieldMapping = null;
            processorMapping = null;
        }
    }

    public int getLineNumber() {
        if (null != csvReader) {
            return csvReader.getLineNumber();
        } else {
            return -1;
        }
    }

    public int getRowNumber() {
        if (null != csvReader) {
            return csvReader.getRowNumber();
        } else {
            return -1;
        }
    }

    public boolean isOpen() {
        return null != csvReader;
    }

    private T createInstance() {
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Cannot instantiate class: " + type, e);
        }
    }

    private void setInstanceProperties(T instance, Map<String, ?> properties) {
        for (Map.Entry<String, ?> propertyEntry : properties.entrySet()) {
            String propertyName = fieldMapping.get(propertyEntry.getKey());
            Object propertyValue = propertyEntry.getValue();
            setInstanceProperty(instance, propertyName, propertyValue);
        }
    }

    private void setInstanceProperty(T instance, String propertyName, Object propertyValue) {
        try {
            if (null != propertyValue) {
                BeanUtils.setProperty(instance, propertyName, propertyValue);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(
                    String.format("Cannot set instance property: %s -> %s", propertyName, propertyValue), e);
        }
    }

    private Map<String, ?> processCells(Map<String, String> cells) {
        Map<String, Object> properties = new HashMap<>();
        CsvContext csvContext = new CsvContext(getLineNumber(), getRowNumber(), 0);

        for (Map.Entry<String, CellProcessor> processorEntry : processorMapping.entrySet()) {
            String columnName = processorEntry.getKey();
            CellProcessor processor = processorEntry.getValue();
            if (null != processor) {
                String cellValue = cells.get(columnName);
                properties.put(columnName, processor.execute(cellValue, csvContext));
            }
        }

        for (Map.Entry<String, String> cellEntry : cells.entrySet()) {
            String columnName = cellEntry.getKey();
            if (!properties.containsKey(columnName)) {
                String cellValue = cellEntry.getValue();
                properties.put(columnName, cellValue);
            }
        }

        return properties;
    }

    private String[] getColumnNames(String[] header, Map<String, String> fieldNameMapping) {
        if (null != fieldNameMapping) {
            List<String> fieldNamesList = new ArrayList<>(header.length);
            for (String column : header) {
                fieldNamesList.add(fieldNameMapping.containsKey(column) ? column : null);
            }
            return fieldNamesList.toArray(new String[fieldNamesList.size()]);
        } else {
            return header;
        }
    }
}
