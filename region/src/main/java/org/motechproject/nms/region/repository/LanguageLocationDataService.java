package org.motechproject.nms.region.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.region.domain.LanguageLocation;

public interface LanguageLocationDataService extends MotechDataService<LanguageLocation> {
    @Lookup
    LanguageLocation findByCode(@LookupField(name = "code") String code);
}
