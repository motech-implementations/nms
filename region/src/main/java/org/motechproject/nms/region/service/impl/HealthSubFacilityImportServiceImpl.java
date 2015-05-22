package org.motechproject.nms.region.service.impl;

import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.repository.HealthFacilityDataService;
import org.motechproject.nms.region.repository.HealthSubFacilityDataService;
import org.motechproject.nms.region.service.HealthSubFacilityImportService;
import org.motechproject.nms.region.utils.GetInstanceByLong;
import org.motechproject.nms.region.utils.GetLong;
import org.motechproject.nms.region.utils.GetString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.HashMap;
import java.util.Map;

@Service("healthSubFacilityImportService")
public class HealthSubFacilityImportServiceImpl extends BaseLocationImportService<HealthSubFacility> implements HealthSubFacilityImportService {

    public static final String SID = "SID";
    public static final String REGIONAL_NAME = "Name_G";
    public static final String NAME = "Name_E";
    public static final String PID = "PID";

    public static final String SID_FIELD = "code";
    public static final String REGIONAL_NAME_FIELD = "regionalName";
    public static final String NAME_FIELD = "name";
    public static final String PID_FIELD = "healthFacility";

    private HealthFacilityDataService healthFacilityDataService;

    @Autowired
    public HealthSubFacilityImportServiceImpl(HealthSubFacilityDataService healthSubFacilityDataService,
                                              HealthFacilityDataService healthFacilityDataService) {
        super(HealthSubFacility.class, healthSubFacilityDataService);
        this.healthFacilityDataService = healthFacilityDataService;
    }

    @Override
    protected Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(SID, new GetLong());
        mapping.put(REGIONAL_NAME, new GetString());
        mapping.put(NAME, new GetString());
        mapping.put(PID, new GetInstanceByLong<HealthFacility>() {
            @Override
            public HealthFacility retrieve(Long value) {
                return healthFacilityDataService.findByCode(value);
            }
        });
        return mapping;
    }

    @Override
    protected Map<String, String> getFieldNameMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put(SID, SID_FIELD);
        mapping.put(REGIONAL_NAME, REGIONAL_NAME_FIELD);
        mapping.put(NAME, NAME_FIELD);
        mapping.put(PID, PID_FIELD);
        return mapping;
    }
}
