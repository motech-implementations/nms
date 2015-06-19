package org.motechproject.nms.region.service.impl;

import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.HashMap;
import java.util.Map;

@Service("districtImportService")
public class DistrictImportServiceImpl extends BaseLocationImportService<District> implements DistrictImportService {

    public static final String DISTRICT_CODE = "DCode";
    public static final String REGIONAL_NAME = "Name_G";
    public static final String NAME = "Name_E";
    public static final String STATE = "StateID";

    public static final String DISTRICT_CODE_FIELD = "code";
    public static final String REGIONAL_NAME_FIELD = "regionalName";
    public static final String NAME_FIELD = "name";
    public static final String STATE_FIELD = "state";

    private StateDataService stateDataService;

    @Autowired
    public DistrictImportServiceImpl(DistrictDataService districtDataService, StateDataService stateDataService) {
        super(District.class, districtDataService);
        this.stateDataService = stateDataService;
    }

    @Override
    protected Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(DISTRICT_CODE, new GetLong());
        mapping.put(REGIONAL_NAME, new GetString());
        mapping.put(NAME, new GetString());
        mapping.put(STATE, mapState(stateDataService));
        return mapping;
    }

    @Override
    protected Map<String, String> getFieldNameMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put(DISTRICT_CODE, DISTRICT_CODE_FIELD);
        mapping.put(REGIONAL_NAME, REGIONAL_NAME_FIELD);
        mapping.put(NAME, NAME_FIELD);
        mapping.put(STATE, STATE_FIELD);
        return mapping;
    }
}
