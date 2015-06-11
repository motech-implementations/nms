package org.motechproject.nms.region.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.region.domain.Village;

public interface VillageDataService extends MotechDataService<Village> {
    @Lookup
    Village findByVcodeAndSvid(@LookupField(name = "vcode") Long vcode, @LookupField(name = "svid") Long svid);

    @Lookup
    Village findBySvid(@LookupField(name = "svid") Long svid);
}
