package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.mcts.contract.AnmAshaRecord;
import org.motechproject.nms.mcts.contract.ChildRecord;
import org.motechproject.nms.mcts.contract.MotherRecord;
import org.motechproject.nms.rch.contract.RchAnmAshaRecord;
import org.motechproject.nms.rch.contract.RchChildRecord;
import org.motechproject.nms.rch.contract.RchMotherRecord;

/**
 * Created by beehyv on 25/7/17.
 */
public interface ActionFinderService {

    public String flwActionFinder(AnmAshaRecord record);

    public String RchFlwActionFinder(RchAnmAshaRecord record);

    public String MotherActionFinder(MotherRecord record);

    public String RchMotherActionFinder(RchMotherRecord record);

    public String ChildActionFinder(ChildRecord record);

    public String RchChildActionFinder(RchChildRecord record);
}
