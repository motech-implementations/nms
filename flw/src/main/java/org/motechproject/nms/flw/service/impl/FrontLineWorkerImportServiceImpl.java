package org.motechproject.nms.flw.service.impl;

import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvInstanceImporter;
import org.motechproject.nms.csv.utils.GetInstanceByLong;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.service.FrontLineWorkerImportService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service("frontLineWorkerImportService")
public class FrontLineWorkerImportServiceImpl implements FrontLineWorkerImportService {

    public static final String ID = "ID";
    public static final String CONTACT_NO ="Contact_No";
    public static final String NAME = "Name";
    public static final String DISTRICT_ID = "District_ID";

    public static final String ID_FIELD = "mctsFlwId";
    public static final String CONTACT_NO_FIELD = "contactNumber";
    public static final String NAME_FIELD = "name";
    public static final String DISTRICT_ID_FIELD = "district";

    private FrontLineWorkerService frontLineWorkerService;
    private DistrictDataService districtDataService;

    @Override
    @Transactional
    public void importData(Reader reader) throws IOException {
        CsvInstanceImporter<FrontLineWorker> csvImporter = new CsvInstanceImporter<>(FrontLineWorker.class);
        try {
            csvImporter.open(reader, getProcessorMapping(), getFieldNameMapping());
            FrontLineWorker instance;
            while (null != (instance = csvImporter.read())) {
                frontLineWorkerService.add(processInstance(instance));
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(createErrorMessage(e.getConstraintViolations(), csvImporter.getRowNumber()), e);
        }
    }

    private FrontLineWorker processInstance(FrontLineWorker instance) {
        if (null != instance.getDistrict()) {
            instance.setLanguageLocation(instance.getDistrict().getLanguageLocation());
        }
        return instance;
    }

    private Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(ID, new GetString());
        mapping.put(CONTACT_NO, new GetLong());
        mapping.put(NAME, new GetString());
        mapping.put(DISTRICT_ID, new Optional(new GetInstanceByLong<District>() {
            @Override
            public District retrieve(Long value) {
                District district = districtDataService.findByCode(value);
                if (null == district) {
                    throw new CsvImportDataException("District does not exists");
                }
                return district;
            }
        }));
        return mapping;
    }

    private Map<String, String> getFieldNameMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put(ID, ID_FIELD);
        mapping.put(CONTACT_NO, CONTACT_NO_FIELD);
        mapping.put(NAME, NAME_FIELD);
        mapping.put(DISTRICT_ID, DISTRICT_ID_FIELD);
        return mapping;
    }

    private String createErrorMessage(Set<ConstraintViolation<?>> violations, int rowNumber) {
        return String.format("CSV instance error [row: %d]: validation failed for instance of type %s, violations: %s",
                rowNumber, FrontLineWorker.class.getName(), ConstraintViolationUtils.toString(violations));
    }

    @Autowired
    public void setFrontLineWorkerService(FrontLineWorkerService frontLineWorkerService) {
        this.frontLineWorkerService = frontLineWorkerService;
    }

    @Autowired
    public void setDistrictDataService(DistrictDataService districtDataService) {
        this.districtDataService = districtDataService;
    }
}
