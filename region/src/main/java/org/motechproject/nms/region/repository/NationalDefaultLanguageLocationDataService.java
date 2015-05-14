package org.motechproject.nms.region.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.region.domain.NationalDefaultLanguageLocation;

public interface NationalDefaultLanguageLocationDataService extends MotechDataService<NationalDefaultLanguageLocation> {
    @Lookup
    NationalDefaultLanguageLocation findByCode(@LookupField(name = "code") Integer code);
}
