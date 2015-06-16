package org.motechproject.nms.csv.utils;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.util.CsvContext;

import java.util.HashMap;
import java.util.Map;

public class Store {

    private Map<String, Object> store;

    public Store() {
        this.store = new HashMap<>();
    }

    public CellProcessor store(String key, CellProcessor processor) {
        return new StoreValue(key, processor);
    }

    public Object get(String key) {
        return store.get(key);
    }

    private class StoreValue implements CellProcessor {

        private String key;
        private CellProcessor processor;

        public StoreValue(String key, CellProcessor processor) {
            this.key = key;
            this.processor = processor;
        }

        @Override
        public Object execute(Object value, CsvContext context) {
            Object processedValue = processor.execute(value, context);
            store.put(key, processedValue);
            return processedValue;
        }
    }
}
