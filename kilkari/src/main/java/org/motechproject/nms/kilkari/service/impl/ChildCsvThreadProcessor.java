package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.RejectionReasons;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.ThreadProcessorObject;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryValueProcessor;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.region.domain.LocationFinder;
import org.motechproject.nms.rejectionhandler.domain.ChildImportRejection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.childRejectionRch;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.convertMapToRchChild;

/**
 * Created by beehyv on 25/4/18.
 */
 public class ChildCsvThreadProcessor implements Callable<ThreadProcessorObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChildCsvThreadProcessor.class);
    private List<Map<String, Object>> recordList;
    private Boolean mctsImport;
    private SubscriptionOrigin importOrigin;
    private LocationFinder locationFinder;
    private MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor;
    private MctsBeneficiaryImportService mctsBeneficiaryImportService;

    public ChildCsvThreadProcessor(List<Map<String, Object>> recordList, Boolean mctsImport,
                                   SubscriptionOrigin importOrigin, LocationFinder locationFinder,
                                   MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor,
                                   MctsBeneficiaryImportService mctsBeneficiaryImportService) {
        this.recordList = recordList;
        this.mctsImport = mctsImport;
        this.importOrigin = importOrigin;
        this.locationFinder = locationFinder;
        this.mctsBeneficiaryValueProcessor = mctsBeneficiaryValueProcessor;
        this.mctsBeneficiaryImportService = mctsBeneficiaryImportService;
    }

    @Override
    public ThreadProcessorObject call() throws Exception { //NOPMD SignatureDeclareThrowsException
        ThreadProcessorObject threadProcessorObject = new ThreadProcessorObject();
        Map<String, Object> rejectedChilds = new HashMap<>();
        Map<String, Object> rejectionStatus = new HashMap<>();
        ChildImportRejection childImportRejection;

        String id;
        String contactNumber;
        String childInstance;
        if (mctsImport) {
            id = KilkariConstants.BENEFICIARY_ID;
            contactNumber = KilkariConstants.MSISDN;
            childInstance = KilkariConstants.MCTS_CHILD;
        } else {
            id = KilkariConstants.RCH_ID;
            contactNumber = KilkariConstants.MOBILE_NO;
            childInstance = KilkariConstants.RCH_CHILD;
        }
        int count = 0;
        Timer timer = new Timer("kid", "kids");
        for(Map<String, Object> record : recordList) {
            count++;
            LOGGER.debug("Started child import for msisdn {} beneficiary_id {}", record.get(contactNumber), record.get(id));

            MctsChild child = mctsImport ? mctsBeneficiaryValueProcessor.getOrCreateChildInstance((String) record.get(id)) : mctsBeneficiaryValueProcessor.getOrCreateRchChildInstance((String) record.get(id), (String) record.get(KilkariConstants.MCTS_ID));
            if (child == null) {
                ChildImportRejection childImportRejection1 = childRejectionRch(convertMapToRchChild(record), false, RejectionReasons.DATA_INTEGRITY_ERROR.toString(), KilkariConstants.CREATE);
                rejectedChilds.put(childImportRejection1.getIdNo(), childImportRejection1);
                rejectionStatus.put(childImportRejection1.getIdNo(), childImportRejection1.getAccepted());
                LOGGER.error("RchId is empty while importing child at msisdn {} beneficiary_id {}", record.get(contactNumber), record.get(id));
                continue;
            }

            String action = (child.getId() == null) ? KilkariConstants.CREATE : KilkariConstants.UPDATE;
            record.put(KilkariConstants.ACTION, action);
            record.put(childInstance, child);

            try {
                childImportRejection = mctsBeneficiaryImportService.importChildRecord(record, importOrigin, locationFinder);
                if (childImportRejection != null) {
                    if (mctsImport) {
                        rejectedChilds.put(childImportRejection.getIdNo(), childImportRejection);
                        rejectionStatus.put(childImportRejection.getIdNo(), childImportRejection.getAccepted());
                    } else {
                        rejectedChilds.put(childImportRejection.getRegistrationNo(), childImportRejection);
                        rejectionStatus.put(childImportRejection.getRegistrationNo(), childImportRejection.getAccepted());
                    }
                }
                if (count % KilkariConstants.PROGRESS_INTERVAL == 0) {
                    LOGGER.debug(KilkariConstants.IMPORTED, timer.frequency(count));
                }
            } catch (RuntimeException e) {
                LOGGER.error("Error while importing child at msisdn {} beneficiary_id {}", record.get(contactNumber), record.get(id), e);
            }
        }
        threadProcessorObject.setRejectedBeneficiaries(rejectedChilds);
        threadProcessorObject.setRejectionStatus(rejectionStatus);
        threadProcessorObject.setRecordsProcessed(count);
        return threadProcessorObject;
    }
}
