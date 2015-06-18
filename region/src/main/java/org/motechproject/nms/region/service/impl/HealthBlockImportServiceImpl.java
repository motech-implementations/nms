package org.motechproject.nms.region.service.impl;

import org.motechproject.nms.csv.utils.GetInstanceByLong;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.csv.utils.Store;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.repository.HealthBlockDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.HealthBlockImportService;
import org.motechproject.nms.region.service.TalukaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service("healthBlockImportService")
public class HealthBlockImportServiceImpl extends BaseLocationImportService<HealthBlock>
        implements HealthBlockImportService {

    public static final String BID = "BID";
    public static final String REGIONAL_NAME = "Name_G";
    public static final String NAME = "Name_E";
    public static final String HQ = "HQ";
    public static final String TALUKA_CODE = "TCode";
    public static final String DISTRICT_CODE = "DCode";
    public static final String STATE_ID = "StateID";

    public static final String BID_FIELD = "code";
    public static final String REGIONAL_NAME_FIELD = "regionalName";
    public static final String NAME_FIELD = "name";
    public static final String HQ_FIELD = "hq";
    public static final String TALUKA_FIELD = "taluka";

    private DistrictService districtService;
    private StateDataService stateDataService;
    private TalukaService talukaService;

    @Autowired
    public HealthBlockImportServiceImpl(HealthBlockDataService healthBlockDataService,
                                        TalukaService talukaService) {
        super(HealthBlock.class, healthBlockDataService);
        this.talukaService = talukaService;
    }

    @Override
    protected Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new LinkedHashMap<>();
        final Store store = new Store();

        mapping.put(BID, new GetLong());
        mapping.put(REGIONAL_NAME, new GetString());
        mapping.put(NAME, new GetString());
        mapping.put(HQ, new GetString());
        mapping.put(STATE_ID, store.store("state", new GetInstanceByLong<State>() {
            @Override
            public State retrieve(Long value) {
                return stateDataService.findByCode(value);
            }
        }));
        mapping.put(DISTRICT_CODE, store.store("district", new GetInstanceByLong<District>() {
            @Override
            public District retrieve(Long value) {
                State state = (State) store.get("state");
                return districtService.findByStateAndCode(state, value);
            }
        }));
        mapping.put(TALUKA_CODE, new GetInstanceByString<Taluka>() {
            @Override
            public Taluka retrieve(String value) {
                District district = (District) store.get("district");
                return talukaService.findByDistrictAndCode(district, value);
            }
        });
        return mapping;
    }

    @Override
    protected Map<String, String> getFieldNameMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put(BID, BID_FIELD);
        mapping.put(REGIONAL_NAME, REGIONAL_NAME_FIELD);
        mapping.put(NAME, NAME_FIELD);
        mapping.put(HQ, HQ_FIELD);
        mapping.put(TALUKA_FIELD, TALUKA_CODE);
        return mapping;
    }
}
