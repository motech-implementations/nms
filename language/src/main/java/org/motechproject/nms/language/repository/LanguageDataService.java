package org.motechproject.nms.language.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.language.domain.Language;


public interface LanguageDataService extends MotechDataService<Language> {
    @Lookup
    Language findByCode(@LookupField(name = "code") Integer code);
}
