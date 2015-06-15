package org.motechproject.nms.region.service.impl;

import org.motechproject.nms.csv.exception.CsvImportException;
import org.motechproject.nms.csv.utils.GetInstanceByLong;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthFacilityType;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.repository.HealthFacilityDataService;
import org.motechproject.nms.region.repository.HealthFacilityTypeDataService;
import org.motechproject.nms.region.service.HealthBlockService;
import org.motechproject.nms.region.service.HealthFacilityImportService;
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
    public static final String BID_FIELD = "healthBlockCode";
    public static final String FACILITY_TYPE_FIELD = "healthFacilityType";

    private HealthFacilityTypeDataService healthFacilityTypeService;
    private HealthBlockService healthBlockService;

    @Autowired
    public HealthFacilityImportServiceImpl(HealthFacilityDataService healthFacilityDataService,
                                           HealthFacilityTypeDataService healthFacilityTypeService,
                                           HealthBlockService healthBlockService) {
        super(HealthFacility.class, healthFacilityDataService);
        this.healthFacilityTypeService = healthFacilityTypeService;
        this.healthBlockService = healthBlockService;
    }

    @Override
    public void addParent(Taluka taluka) {
        addParent(PARENT_TALUKA, taluka);
    }

    @Override
    protected Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(PID, new GetLong());
        mapping.put(REGIONAL_NAME, new GetString());
        mapping.put(NAME, new GetString());
        mapping.put(BID, new GetLong());
        mapping.put(FACILITY_TYPE, new GetInstanceByLong<HealthFacilityType>() {
            @Override
            public HealthFacilityType retrieve(Long value) {
                return healthFacilityTypeService.findByCode(value);
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

    @Override
    protected void postReadStep(HealthFacility healthFacility) {
        Taluka taluka = (Taluka) getParent(PARENT_TALUKA);
        if (taluka == null) {
            throw new CsvImportException("No taluka provided!");
        }

        HealthBlock healthBlock = healthBlockService.findByTalukaAndCode(taluka,
                healthFacility.getHealthBlockCode());
        if (healthBlock == null) {
            throw new CsvImportException(String.format("No such healthBlock '%d' for taluka '%s'",
                    healthFacility.getHealthBlockCode(), taluka.getName()));
        }
        healthFacility.setHealthBlock(healthBlock);
    }
}
