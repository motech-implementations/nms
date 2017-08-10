package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.nms.rejectionhandler.domain.FlwImportRejection;
import org.motechproject.nms.rejectionhandler.repository.FlwImportRejectionDataService;
import org.motechproject.nms.rejectionhandler.service.FlwRejectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by vishnu on 15/7/17.
 */
@Service("flwRejectionService")
public class FlwRejectionServiceImpl implements FlwRejectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlwRejectionServiceImpl.class);

    @Autowired
    private FlwImportRejectionDataService flwImportRejectionDataService;

    @Override
    public FlwImportRejection findByFlwIdAndStateId(Long flwId, Long stateId) {
        return flwImportRejectionDataService.findByFlwIdAndStateId(flwId, stateId);
    }

    @Override //NO CHECKSTYLE CyclomaticComplexity
    public void createUpdate(FlwImportRejection flwImportRejection) {
        if (flwImportRejection.getFlwId() != null && flwImportRejection.getStateId() != null) {
            FlwImportRejection flwImportRejection1 = findByFlwIdAndStateId(flwImportRejection.getFlwId(), flwImportRejection.getStateId());

            if (flwImportRejection1 == null && !flwImportRejection.getAccepted()) {
                flwImportRejectionDataService.create(flwImportRejection);
            } else if (flwImportRejection1 == null && flwImportRejection.getAccepted()) {
                LOGGER.debug(String.format("There is no rejection data for flwId %s and stateId %s", flwImportRejection.getFlwId().toString(), flwImportRejection.getStateId().toString()));
            } else if (flwImportRejection1 != null && !flwImportRejection1.getAccepted()) {
                flwImportRejection.setId(flwImportRejection1.getId());
                flwImportRejectionDataService.update(flwImportRejection);
            } else if (flwImportRejection1 != null && flwImportRejection1.getAccepted()) {
                flwImportRejection.setId(flwImportRejection1.getId());
                flwImportRejectionDataService.update(flwImportRejection);
            }
        }
    }
}
