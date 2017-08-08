package org.motechproject.nms.rch.repository;

import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.rch.domain.RchImportFacilitator;
import org.motechproject.nms.rch.domain.RchUserType;

import java.util.List;

/**
 * Created by vishnu on 24/7/17.
 */
public interface RchImportFacilitatorDataService extends MotechDataService<RchImportFacilitator> {

    @Lookup
    List<RchImportFacilitator> getByImportDateAndUsertype(@LookupField(name = "importDate") LocalDate importDate,
                                                          @LookupField(name = "userType") RchUserType userType);

}



