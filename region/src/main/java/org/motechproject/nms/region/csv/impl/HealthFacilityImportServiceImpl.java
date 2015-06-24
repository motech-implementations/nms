package org.motechproject.nms.region.csv.impl;

import org.motechproject.nms.csv.utils.GetInstanceByLong;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.csv.utils.Store;
import org.motechproject.nms.region.csv.HealthFacilityImportService;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthFacilityType;
import org.motechproject.nms.region.repository.HealthFacilityDataService;
import org.motechproject.nms.region.repository.HealthFacilityTypeDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.HealthBlockService;
import org.motechproject.nms.region.service.TalukaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service("healthFacilityImportService")
public class HealthFacilityImportServiceImpl extends BaseLocationImportService<HealthFacility> implements HealthFacilityImportService {

    public static final String PID = "PID";
    public static final String REGIONAL_NAME = "Name_G";
    public static final String NAME = "Name_E";
    public static final String BID = "BID";
    public static final String FACILITY_TYPE = "Facility_Type";
    public static final String TALUKA_CODE = "TCode";
    public static final String DISTRICT_CODE = "DCode";
    public static final String STATE_ID = "StateID";

    public static final String PID_FIELD = "code";
    public static final String REGIONAL_NAME_FIELD = "regionalName";
    public static final String NAME_FIELD = "name";
    public static final String BID_FIELD = "healthBlock";
    public static final String FACILITY_TYPE_FIELD = "healthFacilityType";

    private HealthFacilityTypeDataService healthFacilityTypeService;
    private HealthBlockService healthBlockService;
    private DistrictService districtService;
    private StateDataService stateDataService;
    private TalukaService talukaService;

    @Autowired
    public HealthFacilityImportServiceImpl(HealthFacilityDataService healthFacilityDataService,
                                           HealthFacilityTypeDataService healthFacilityTypeService,
                                           HealthBlockService healthBlockService,
                                           DistrictService districtService,
                                           StateDataService stateDataService,
                                           TalukaService talukaService) {
        super(HealthFacility.class, healthFacilityDataService);
        this.healthFacilityTypeService = healthFacilityTypeService;
        this.healthBlockService = healthBlockService;
        this.districtService = districtService;
        this.stateDataService = stateDataService;
        this.talukaService = talukaService;
    }

    @Override
    protected Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new LinkedHashMap<>();
        final Store store = new Store();

        mapping.put(PID, new GetLong());
        mapping.put(REGIONAL_NAME, new GetString());
        mapping.put(NAME, new GetString());
        mapping.put(FACILITY_TYPE, new GetInstanceByLong<HealthFacilityType>() {
            @Override
            public HealthFacilityType retrieve(Long value) {
                return healthFacilityTypeService.findByCode(value);
            }
        });
        mapping.put(STATE_ID, store.store(STATE, mapState(stateDataService)));
        mapping.put(DISTRICT_CODE, store.store(DISTRICT, mapDistrict(store, districtService)));
        mapping.put(TALUKA_CODE, store.store(TALUKA, mapTaluka(store, talukaService)));
        mapping.put(BID, mapHealthBlock(store, healthBlockService));

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
        mapping.put(STATE_ID, null);
        mapping.put(DISTRICT_CODE, null);
        mapping.put(TALUKA_CODE, null);
        return mapping;
    }
}
