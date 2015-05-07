package org.motechproject.nms.region.language.repository;

import org.motechproject.nms.region.language.domain.CircleLanguage;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;

import java.util.List;

public interface CircleLanguageDataService extends MotechDataService<CircleLanguage> {
    @Lookup
    List<CircleLanguage> findByCircle(@LookupField(name = "circle") String circle);
}
