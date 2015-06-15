package org.motechproject.nms.region.service.impl;

import org.motechproject.nms.csv.exception.CsvImportException;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.repository.HealthSubFacilityDataService;
import org.motechproject.nms.region.service.HealthFacilityService;
import org.motechproject.nms.region.service.HealthSubFacilityImportService;
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
    public static final String PID_FIELD = "healthFacilityCode";

    private HealthFacilityService healthFacilityService;

    @Autowired
    public HealthSubFacilityImportServiceImpl(HealthSubFacilityDataService healthSubFacilityDataService,
                                              HealthFacilityService healthFacilityService) {
        super(HealthSubFacility.class, healthSubFacilityDataService);
        this.healthFacilityService = healthFacilityService;
    }

    @Override
    public void addParent(HealthBlock healthBlock) {
        addParent(PARENT_HEALTH_BLOCK, healthBlock);
    }

    @Override
    protected Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(SID, new GetLong());
        mapping.put(REGIONAL_NAME, new GetString());
        mapping.put(NAME, new GetString());
        mapping.put(PID, new GetLong());
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

    @Override
    protected void postReadStep(HealthSubFacility healthSubFacility) {
        HealthBlock healthBlock = (HealthBlock) getParent(PARENT_HEALTH_BLOCK);
        if (healthBlock == null) {
            throw new CsvImportException("No healthBlock provided!");
        }

        HealthFacility healthFacility = healthFacilityService.findByHealthBlockAndCode(healthBlock,
                healthSubFacility.getHealthFacilityCode());
        if (healthFacility == null) {
            throw new CsvImportException(String.format("No such healthFacility '%d' for healthBlock '%s'",
                    healthSubFacility.getHealthFacilityCode(), healthBlock.getName()));
        }
        healthSubFacility.setHealthFacility(healthFacility);
    }
}
