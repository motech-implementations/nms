package org.motechproject.nms.region.csv.impl;

import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.csv.utils.Store;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.repository.VillageDataService;
import org.motechproject.nms.region.csv.CensusVillageImportService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.TalukaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service("censusVillageImportService")
public class CensusVillageImportServiceImpl extends BaseLocationImportService<Village>
        implements CensusVillageImportService {

    public static final String VILLAGE_CODE = "VCode";
    public static final String REGIONAL_NAME = "Name_G";
    public static final String NAME = "Name_E";
    public static final String TALUKA_CODE = "TCode";
    public static final String DISTRICT_CODE = "DCode";
    public static final String STATE_ID = "StateID";

    public static final String VILLAGE_CODE_FIELD = "vcode";
    public static final String REGIONAL_NAME_FIELD = "regionalName";
    public static final String NAME_FIELD = "name";
    public static final String TALUKA_FIELD = "taluka";

    private DistrictService districtService;
    private StateDataService stateDataService;
    private TalukaService talukaService;

    @Autowired
    public CensusVillageImportServiceImpl(VillageDataService villageDataService,
                                          TalukaService talukaService,
                                          DistrictService districtService,
                                          StateDataService stateDataService) {
        super(Village.class, villageDataService);
        this.talukaService = talukaService;
        this.districtService = districtService;
        this.stateDataService = stateDataService;
    }

    @Override
    protected Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new LinkedHashMap<>();
        final Store store = new Store();

        mapping.put(VILLAGE_CODE, new GetLong());
        mapping.put(REGIONAL_NAME, new GetString());
        mapping.put(NAME, new GetString());
        mapping.put(STATE_ID, store.store(STATE, mapState(stateDataService)));
        mapping.put(DISTRICT_CODE, store.store(DISTRICT, mapDistrict(store, districtService)));
        mapping.put(TALUKA_CODE, mapTaluka(store, talukaService));
        return mapping;
    }

    @Override
    protected Map<String, String> getFieldNameMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put(VILLAGE_CODE, VILLAGE_CODE_FIELD);
        mapping.put(REGIONAL_NAME, REGIONAL_NAME_FIELD);
        mapping.put(NAME, NAME_FIELD);
        mapping.put(TALUKA_CODE, TALUKA_FIELD);
        mapping.put(STATE_ID, null);
        mapping.put(DISTRICT_CODE, null);
        return mapping;
    }
}
