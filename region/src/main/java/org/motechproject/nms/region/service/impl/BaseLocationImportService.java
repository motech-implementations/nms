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
import java.util.Map;
import java.util.Set;

public abstract class BaseLocationImportService<T> {

    private Class<T> type;
    private MotechDataService<T> dataService;

    public BaseLocationImportService(Class<T> type, MotechDataService<T> dataService) {
        this.type = type;
        this.dataService = dataService;
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
                dataService.create(instance);
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(createErrorMessage(e.getConstraintViolations(), csvImporter.getRowNumber()), e);
        }
    }

    protected abstract Map<String, CellProcessor> getProcessorMapping();

    protected abstract Map<String, String> getFieldNameMapping();

    private String createErrorMessage(Set<ConstraintViolation<?>> violations, int rowNumber) {
        if (CollectionUtils.isNotEmpty(violations)) {
            return String.format("CSV instance error [row: %d]: validation failed for instance of type %s, violations: %s", rowNumber, type.getName(), ConstraintViolationUtils.toString(violations));
        } else {
            return String.format("CSV instance error [row: %d]", rowNumber);
        }
    }

}
