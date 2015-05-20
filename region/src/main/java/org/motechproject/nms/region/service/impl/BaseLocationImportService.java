package org.motechproject.nms.region.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.region.exception.CsvImportDataException;
import org.motechproject.nms.region.service.LocationDataImportService;
import org.motechproject.nms.region.utils.CsvImporter;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.ift.CellProcessor;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

public abstract class BaseLocationImportService<T> implements LocationDataImportService {

    private Class<T> type;
    private MotechDataService<T> dataService;

    public BaseLocationImportService(Class<T> type, MotechDataService<T> dataService) {
        this.type = type;
        this.dataService = dataService;
    }

    @Override
    @Transactional
    public void importData(Reader reader) throws IOException {
        CsvImporter<T> csvImporter = new CsvImporter<>(type);
        try {
            csvImporter.open(reader, getFieldNameMapping(), getProcessorMapping());
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
            StringBuilder builder = new StringBuilder();
            for (ConstraintViolation<?> violation : violations) {
                builder.append(String.format("{'%s': %s}", violation.getPropertyPath(), violation.getMessage()));
            }
            return String.format("CSV instance error [row: %d]: validation failed for instance of type %s, violations: %s", rowNumber, type.getName(), builder);
        } else {
            return String.format("CSV instance error [row: %d]", rowNumber);
        }
    }
}
