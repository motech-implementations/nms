package org.motechproject.nms.flw.service.impl;

import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Simple implementation of the {@link org.motechproject.nms.flw.service.FrontLineWorkerService} interface.
 */
@Service("frontLineWorkerService")
public class FrontLineWorkerServiceImpl implements FrontLineWorkerService {
    private FrontLineWorkerDataService frontLineWorkerDataService;

    @Autowired
    public FrontLineWorkerServiceImpl(FrontLineWorkerDataService frontLineWorkerDataService) {
        this.frontLineWorkerDataService = frontLineWorkerDataService;
    }

    @Override
    public void add(FrontLineWorker record) {

        // TODO: also check for FLWDesignation, once we add that field
        // TODO: find out which language/location fields are mandatory
        if ((record.getName() != null) && (record.getContactNumber() != null) &&
                (record.getLanguageLocation() != null) && (record.getDistrict() != null)) {

            // the record was added via CSV upload and the FLW hasn't called the service yet
            record.setStatus(FrontLineWorkerStatus.INACTIVE);

        } else if (record.getContactNumber() != null) {

            // the record was added when the FLW called the IVR service for the first time
            record.setStatus(FrontLineWorkerStatus.ANONYMOUS);

        }

        frontLineWorkerDataService.create(record);
    }

    @Override
    public FrontLineWorker getByContactNumber(Long contactNumber) {
        return frontLineWorkerDataService.findByContactNumber(contactNumber);
    }

    @Override
    public List<FrontLineWorker> getRecords() {
        return frontLineWorkerDataService.retrieveAll();
    }

    /**
     * Update FrontLineWorker. If specific fields are added to the record (name, contactNumber, languageLocation,
     * district, designation), the FrontLineWorker's status will also be updated.
     * @param record The FrontLineWorker to update
     */
    @Override
    @Transactional
    public void update(FrontLineWorker record) {

        FrontLineWorker retrievedFlw = frontLineWorkerDataService.findByContactNumber(record.getContactNumber());
        FrontLineWorkerStatus oldStatus = retrievedFlw.getStatus();

        if (record.getStatus() == FrontLineWorkerStatus.INVALID) {
            // if the caller sets the status to INVALID, that takes precedence over any other status change
            frontLineWorkerDataService.update(record);
            return;
        }
        if (oldStatus == FrontLineWorkerStatus.ANONYMOUS) {
            // if the FLW was ANONYMOUS and the required fields get added, update her status to ACTIVE

            // TODO: also check for FLWDesignation once we get spec clarity on what that is
            if ((record.getName() != null) && (record.getContactNumber() != null) &&
                    (record.getLanguageLocation() != null) && (record.getDistrict() != null)) {

                record.setStatus(FrontLineWorkerStatus.ACTIVE);
            }
        }

        frontLineWorkerDataService.update(record);
    }

    @Override
    public void delete(FrontLineWorker record) {
        frontLineWorkerDataService.delete(record);
    }
}
