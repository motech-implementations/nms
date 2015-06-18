package org.motechproject.nms.region.service.impl;

import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.exception.CsvImportException;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.repository.VillageDataService;
import org.motechproject.nms.region.service.TalukaService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseVillageImportService extends BaseLocationImportService<Village> {

    private TalukaService talukaService;

    @Autowired
    public BaseVillageImportService(VillageDataService villageDataService, TalukaService talukaService) {
        super(Village.class, villageDataService);
        this.talukaService = talukaService;
    }

    @Override
    protected void postReadStep(Village village) {
        Taluka taluka;

        District district = (District) getParent(PARENT_DISTRICT);
        if (district == null) {
            throw new CsvImportException("No district provided!");
        }

        try {
            taluka = talukaService.findByDistrictAndCode(district, village.getTalukaCode());
        } catch (NumberFormatException e) {
            throw new CsvImportDataException(String.format("Invalid taluka: %s", village.getTalukaCode()), e);
        }
        if (taluka == null) {
            throw new CsvImportException(String.format("No such taluka '%s' for district '%s'",
                    village.getTalukaCode(), district.getName()));
        }
        village.setTaluka(taluka);
    }
}
