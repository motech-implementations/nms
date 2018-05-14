package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.contract.ChildRecord;
import org.motechproject.nms.kilkari.contract.MotherRecord;
import org.motechproject.nms.kilkari.contract.RchMotherRecord;

/**
 * Created by beehyv on 25/7/17.
 */
public interface ActionFinderService {

    String motherActionFinder(MotherRecord record);

    String rchMotherActionFinder(RchMotherRecord record);

    String childActionFinder(ChildRecord record);
}
