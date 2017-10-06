package org.motechproject.nms.api.web.service;

import org.motechproject.nms.api.web.contract.AddFlwRequest;
import org.motechproject.nms.api.web.contract.AddRchFlwRequest;

/**
 * Created by vishnu on 22/9/17.
 */
public interface FlwCsvService {

    StringBuilder csvUploadMcts(AddFlwRequest addFlwRequest);

    void persistFlwMcts(AddFlwRequest addFlwRequest);

    StringBuilder csvUploadRch(AddRchFlwRequest addRchFlwRequest);

    void persistFlwRch(AddRchFlwRequest addRchFlwRequest);

    void csvRejectionsMcts(String fieldName, AddFlwRequest addFlwRequest);

    void csvRejectionsRch(String fieldName, AddRchFlwRequest addRchFlwRequest);
}
