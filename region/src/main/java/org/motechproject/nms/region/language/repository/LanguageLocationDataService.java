package org.motechproject.nms.region.language.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.region.language.domain.LanguageLocation;

import java.util.List;

public interface LanguageLocationDataService extends MotechDataService<LanguageLocation> {
    @Lookup
    List<LanguageLocation> findByCircle(@LookupField(name = "circle") String circle);
}
