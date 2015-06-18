package org.motechproject.nms.region.service.impl;

import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.repository.VillageDataService;
import org.motechproject.nms.region.service.CensusVillageImportService;
import org.motechproject.nms.region.service.TalukaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.HashMap;
import java.util.Map;

@Service("censusVillageImportService")
public class CensusVillageImportServiceImpl extends BaseVillageImportService
        implements CensusVillageImportService {

    public static final String VILLAGE_CODE = "VCode";
    public static final String REGIONAL_NAME = "Name_G";
    public static final String NAME = "Name_E";
    public static final String TALUKA_CODE = "TCode";

    public static final String VILLAGE_CODE_FIELD = "vcode";
    public static final String REGIONAL_NAME_FIELD = "regionalName";
    public static final String NAME_FIELD = "name";
    public static final String TALUKA_CODE_FIELD = "talukaCode";


    @Autowired
    public CensusVillageImportServiceImpl(VillageDataService villageDataService, TalukaService talukaService) {
        super(villageDataService, talukaService);
    }

    @Override
    public void addParent(District district) {
        addParent(PARENT_DISTRICT, district);
    }

    @Override
    protected Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(VILLAGE_CODE, new GetLong());
        mapping.put(REGIONAL_NAME, new GetString());
        mapping.put(NAME, new GetString());
        mapping.put(TALUKA_CODE, new GetString());
        return mapping;
    }

    @Override
    protected Map<String, String> getFieldNameMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put(VILLAGE_CODE, VILLAGE_CODE_FIELD);
        mapping.put(REGIONAL_NAME, REGIONAL_NAME_FIELD);
        mapping.put(NAME, NAME_FIELD);
        mapping.put(TALUKA_CODE, TALUKA_CODE_FIELD);
        return mapping;
    }
}
