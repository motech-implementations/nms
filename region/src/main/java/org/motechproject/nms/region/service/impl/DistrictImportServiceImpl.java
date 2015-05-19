package org.motechproject.nms.region.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.exception.CsvImportDataException;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictImportService;
import org.motechproject.nms.region.utils.CsvImporter;
import org.motechproject.nms.region.utils.GetLong;
import org.motechproject.nms.region.utils.GetStateByCode;
import org.motechproject.nms.region.utils.GetString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.ift.CellProcessor;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service("districtImportService")
public class DistrictImportServiceImpl implements DistrictImportService {

    public static final String DISTRICT_CODE = "DCode";
    public static final String REGIONAL_NAME = "Name_G";
    public static final String NAME = "Name_E";
    public static final String STATE = "StateID";

    public static final String DISTRICT_CODE_FIELD = "code";
    public static final String REGIONAL_NAME_FIELD = "regionalName";
    public static final String NAME_FIELD = "name";
    public static final String STATE_FIELD = "state";

    private DistrictDataService districtDataService;
    private StateDataService stateDataService;

    @Override
    @Transactional
    public void importData(Reader reader) throws IOException {
        int rowNumber = 0;
        try {
            CsvImporter<District> csvImporter = new CsvImporter<>(District.class);
            csvImporter.open(reader, getFieldNameMapping(), getProcessorMapping());
            District district;
            while (null != (district = csvImporter.read())) {
                rowNumber = csvImporter.getRowNumber();
                districtDataService.create(district);
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(createMessage(e.getConstraintViolations(), rowNumber), e);
        }
    }

    private String createMessage(Set<ConstraintViolation<?>> violations, int rowNumber) {
        if (CollectionUtils.isNotEmpty(violations)) {
            Class<?> instanceType = ((ConstraintViolation<?>) CollectionUtils.get(violations, 0)).getRootBeanClass();
            StringBuilder builder = new StringBuilder();
            for (ConstraintViolation<?> violation : violations) {
                builder.append(String.format("{'%s': %s}", violation.getPropertyPath(), violation.getMessage()));
            }
            return String.format("CSV instance error [row: %d]: validation failed for instance of type %s, violations: %s", rowNumber, instanceType, builder);
        } else {
            return String.format("CSV instance error [row: %d]", rowNumber);
        }
    }

    private Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(DISTRICT_CODE, new GetLong());
        mapping.put(REGIONAL_NAME, new GetString());
        mapping.put(NAME, new GetString());
        mapping.put(STATE, new GetStateByCode(stateDataService));
        return mapping;
    }

    private Map<String, String> getFieldNameMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put(DISTRICT_CODE, DISTRICT_CODE_FIELD);
        mapping.put(REGIONAL_NAME, REGIONAL_NAME_FIELD);
        mapping.put(NAME, NAME_FIELD);
        mapping.put(STATE, STATE_FIELD);
        return mapping;
    }

    @Autowired
    public void setDistrictDataService(DistrictDataService districtDataService) {
        this.districtDataService = districtDataService;
    }

    @Autowired
    public void setStateDataService(StateDataService stateDataService) {
        this.stateDataService = stateDataService;
    }
}
