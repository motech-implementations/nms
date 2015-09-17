package org.motechproject.nms.region.csv.impl;

import org.motechproject.nms.csv.utils.GetInteger;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.csv.utils.Store;
import org.motechproject.nms.region.csv.TalukaImportService;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.StateService;
import org.motechproject.nms.region.service.TalukaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service("talukaImportService")
public class TalukaImportServiceImpl extends BaseLocationImportService<Taluka>
        implements TalukaImportService {

    public static final String TALUKA_CODE = "TCode";
    public static final String IDENTITY = "ID";
    public static final String REGIONAL_NAME = "Name_G";
    public static final String NAME = "Name_E";
    public static final String DISTRICT_CODE = "DCode";
    public static final String STATE_ID = "StateID";

    public static final String TALUKA_CODE_FIELD = "code";
    public static final String IDENTITY_FIELD = "identity";
    public static final String REGIONAL_NAME_FIELD = "regionalName";
    public static final String NAME_FIELD = "name";
    public static final String DISTRICT_FIELD = "district";

    private TalukaService talukaService;
    private DistrictService districtService;
    private StateService stateDataService;

    @Autowired
    public TalukaImportServiceImpl(DistrictService districtService, StateService stateDataService,
                                   TalukaService talukaService) {
        super(Taluka.class);
        this.districtService = districtService;
        this.stateDataService = stateDataService;
        this.talukaService = talukaService;
    }

    @Override
    protected void createOrUpdateInstance(Taluka instance) {
        Taluka existing = talukaService.findByDistrictAndCode(instance.getDistrict(), instance.getCode());

        if (existing != null) {
            existing.setIdentity(instance.getIdentity());
            existing.setRegionalName(instance.getRegionalName());
            existing.setName(instance.getName());

            talukaService.update(existing);
        } else {
            talukaService.create(instance);
        }
    }

    @Override
    protected Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new LinkedHashMap<>();
        final Store store = new Store();

        mapping.put(TALUKA_CODE, new GetString());
        mapping.put(IDENTITY, new GetInteger());
        mapping.put(REGIONAL_NAME, new GetString());
        mapping.put(NAME, new GetString());
        mapping.put(STATE_ID, store.store(STATE, mapState(stateDataService)));
        mapping.put(DISTRICT_CODE, mapDistrict(store, districtService));

        return mapping;
    }

    @Override
    protected Map<String, String> getFieldNameMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put(TALUKA_CODE, TALUKA_CODE_FIELD);
        mapping.put(IDENTITY, IDENTITY_FIELD);
        mapping.put(REGIONAL_NAME, REGIONAL_NAME_FIELD);
        mapping.put(DISTRICT_CODE, DISTRICT_FIELD);
        mapping.put(NAME, NAME_FIELD);
        mapping.put(STATE_ID, STATE_ID);
        return mapping;
    }
}
