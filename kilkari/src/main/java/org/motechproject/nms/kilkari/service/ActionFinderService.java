package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.contract.AnmAshaRecord;
import org.motechproject.nms.kilkari.contract.ChildRecord;
import org.motechproject.nms.kilkari.contract.MotherRecord;
import org.motechproject.nms.kilkari.contract.RchAnmAshaRecord;
import org.motechproject.nms.kilkari.contract.RchChildRecord;
import org.motechproject.nms.kilkari.contract.RchMotherRecord;

/**
 * Created by beehyv on 25/7/17.
 */
public interface ActionFinderService {

    public String MotherActionFinder(MotherRecord record);

    public String RchMotherActionFinder(RchMotherRecord record);

    public String ChildActionFinder(ChildRecord record);

    public String RchChildActionFinder(RchChildRecord record);
}
