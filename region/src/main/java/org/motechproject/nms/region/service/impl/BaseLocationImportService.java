package org.motechproject.nms.region.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvInstanceImporter;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.ift.CellProcessor;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class BaseLocationImportService<T> {

    public static final String PARENT_STATE = "state";
    public static final String PARENT_DISTRICT = "district";
    public static final String PARENT_TALUKA = "taluka";
    public static final String PARENT_HEALTH_BLOCK = "healthBlock";

    private Class<T> type;
    private MotechDataService<T> dataService;
    private Map<String, Object> parents;

    public BaseLocationImportService(Class<T> type, MotechDataService<T> dataService) {
        this.type = type;
        this.dataService = dataService;
        this.parents = new HashMap<>();
    }

    public void addParent(String k, Object v) {
        parents.put(k, v);
    }

    public Object getParent(String k) {
        return parents.get(k);
    }

    @Transactional
    public void importData(Reader reader) throws IOException {
        CsvInstanceImporter<T> csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(getProcessorMapping())
                .setFieldNameMapping(getFieldNameMapping())
                .createAndOpen(reader, type);
        try {
            T instance;
            while (null != (instance = csvImporter.read())) {
                postReadStep(instance);
                dataService.create(instance);
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(createErrorMessage(e.getConstraintViolations(), csvImporter.getRowNumber()), e);
        }
    }

    protected abstract Map<String, CellProcessor> getProcessorMapping();

    protected abstract Map<String, String> getFieldNameMapping();

    protected void postReadStep(T instance) { }

    private String createErrorMessage(Set<ConstraintViolation<?>> violations, int rowNumber) {
        if (CollectionUtils.isNotEmpty(violations)) {
            return String.format("CSV instance error [row: %d]: validation failed for instance of type %s, violations: %s", rowNumber, type.getName(), ConstraintViolationUtils.toString(violations));
        } else {
            return String.format("CSV instance error [row: %d]", rowNumber);
        }
    }

}
