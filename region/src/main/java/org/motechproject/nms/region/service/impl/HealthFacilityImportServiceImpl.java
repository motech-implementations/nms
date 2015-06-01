package org.motechproject.nms.region.service.impl;

import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthFacilityType;
import org.motechproject.nms.region.repository.HealthBlockDataService;
import org.motechproject.nms.region.repository.HealthFacilityDataService;
import org.motechproject.nms.region.repository.HealthFacilityTypeDataService;
import org.motechproject.nms.region.service.HealthFacilityImportService;
import org.motechproject.nms.region.utils.GetInstanceByLong;
import org.motechproject.nms.region.utils.GetLong;
import org.motechproject.nms.region.utils.GetString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.HashMap;
import java.util.Map;

@Service("healthFacilityImportService")
public class HealthFacilityImportServiceImpl extends BaseLocationImportService<HealthFacility> implements HealthFacilityImportService {

    public static final String PID = "PID";
    public static final String REGIONAL_NAME = "Name_G";
    public static final String NAME = "Name_E";
    public static final String BID = "BID";
    public static final String FACILITY_TYPE = "Facility_Type";

    public static final String PID_FIELD = "code";
    public static final String REGIONAL_NAME_FIELD = "regionalName";
    public static final String NAME_FIELD = "name";
    public static final String BID_FIELD = "healthBlock";
    public static final String FACILITY_TYPE_FIELD = "healthFacilityType";

    private HealthBlockDataService healthBlockDataService;
    private HealthFacilityTypeDataService healthFacilityTypeDataService;

    @Autowired
    public HealthFacilityImportServiceImpl(HealthFacilityDataService healthFacilityDataService,
                                           HealthBlockDataService healthBlockDataService,
                                           HealthFacilityTypeDataService healthFacilityTypeDataService) {
        super(HealthFacility.class, healthFacilityDataService);
        this.healthBlockDataService = healthBlockDataService;
        this.healthFacilityTypeDataService = healthFacilityTypeDataService;
    }

    @Override
    protected Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(PID, new GetLong());
        mapping.put(REGIONAL_NAME, new GetString());
        mapping.put(NAME, new GetString());
        mapping.put(BID, new GetInstanceByLong<HealthBlock>() {
            @Override
            public HealthBlock retrieve(Long value) {
                return healthBlockDataService.findByCode(value);
            }
        });
        mapping.put(FACILITY_TYPE, new GetInstanceByLong<HealthFacilityType>() {
            @Override
            public HealthFacilityType retrieve(Long value) {
                return healthFacilityTypeDataService.findByCode(value);
            }
        });
        return mapping;
    }

    @Override
    protected Map<String, String> getFieldNameMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put(PID, PID_FIELD);
        mapping.put(REGIONAL_NAME, REGIONAL_NAME_FIELD);
        mapping.put(NAME, NAME_FIELD);
        mapping.put(BID, BID_FIELD);
        mapping.put(FACILITY_TYPE, FACILITY_TYPE_FIELD);
        return mapping;
    }
}
