package org.motechproject.nms.region.csv.impl;

import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.csv.utils.Store;
import org.motechproject.nms.region.csv.HealthSubFacilityImportService;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.repository.HealthSubFacilityDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.HealthBlockService;
import org.motechproject.nms.region.service.HealthFacilityService;
import org.motechproject.nms.region.service.TalukaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service("healthSubFacilityImportService")
public class HealthSubFacilityImportServiceImpl extends BaseLocationImportService<HealthSubFacility>
        implements HealthSubFacilityImportService {

    public static final String SID = "SID";
    public static final String REGIONAL_NAME = "Name_G";
    public static final String NAME = "Name_E";
    public static final String PID = "PID";
    public static final String BID = "BID";
    public static final String TALUKA_CODE = "TCode";
    public static final String DISTRICT_CODE = "DCode";
    public static final String STATE_ID = "StateID";

    public static final String SID_FIELD = "code";
    public static final String REGIONAL_NAME_FIELD = "regionalName";
    public static final String NAME_FIELD = "name";
    public static final String PID_FIELD = "healthFacility";

    private HealthBlockService healthBlockService;
    private DistrictService districtService;
    private StateDataService stateDataService;
    private TalukaService talukaService;
    private HealthFacilityService healthFacilityService;

    @Autowired
    public HealthSubFacilityImportServiceImpl(
            HealthSubFacilityDataService healthSubFacilityDataService,
            HealthFacilityService healthFacilityService,
            HealthBlockService healthBlockService,
            DistrictService districtService,
            StateDataService stateDataService,
            TalukaService talukaService) {
        super(HealthSubFacility.class, healthSubFacilityDataService);
        this.healthFacilityService = healthFacilityService;
        this.healthBlockService = healthBlockService;
        this.districtService = districtService;
        this.stateDataService = stateDataService;
        this.talukaService = talukaService;
    }

    @Override
    protected Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new LinkedHashMap<>();
        final Store store = new Store();

        mapping.put(SID, new GetLong());
        mapping.put(REGIONAL_NAME, new GetString());
        mapping.put(NAME, new GetString());
        mapping.put(STATE_ID, store.store(STATE, mapState(stateDataService)));
        mapping.put(DISTRICT_CODE, store.store(DISTRICT, mapDistrict(store, districtService)));
        mapping.put(TALUKA_CODE, store.store(TALUKA, mapTaluka(store, talukaService)));
        mapping.put(BID, store.store(HEALTH_BLOCK, mapHealthBlock(store, healthBlockService)));
        mapping.put(PID, mapHealthFacility(store, healthFacilityService));

        return mapping;
    }

    @Override
    protected Map<String, String> getFieldNameMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put(SID, SID_FIELD);
        mapping.put(REGIONAL_NAME, REGIONAL_NAME_FIELD);
        mapping.put(NAME, NAME_FIELD);
        mapping.put(PID, PID_FIELD);
        mapping.put(STATE_ID, null);
        mapping.put(DISTRICT_CODE, null);
        mapping.put(TALUKA_CODE, null);
        mapping.put(BID, null);
        return mapping;
    }
}
