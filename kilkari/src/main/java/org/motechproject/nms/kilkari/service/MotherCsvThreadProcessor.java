package org.motechproject.nms.kilkari.service;

import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.kilkari.domain.*;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.service.impl.SubscriptionServiceImpl;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.region.domain.LocationFinder;
import org.motechproject.nms.rejectionhandler.domain.MotherImportRejection;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.convertMapToRchMother;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.motherRejectionRch;

/**
 * Created by beehyv on 8/5/18.
 */
public class MotherCsvThreadProcessor implements Callable<ThreadProcessorObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MotherCsvThreadProcessor.class);
    private List<Map<String, Object>> recordList;
    private Boolean mctsImport;
    private SubscriptionOrigin importOrigin;
    private LocationFinder locationFinder;
    private MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor;
    private MctsBeneficiaryImportService mctsBeneficiaryImportService;
    private long chunkSize;
    private static long savedRecords=0;
    private static boolean isCapacityExceededCheck=false;
//    private SubscriptionDataService subscriptionDataService;
//
//    @Autowired
//    @Qualifier("kilkariSettings")
//    private SettingsFacade settingsFacade;

    public MotherCsvThreadProcessor(List<Map<String, Object>> recordList, Boolean mctsImport,
                                    SubscriptionOrigin importOrigin, LocationFinder locationFinder,
                                    MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor,
                                    MctsBeneficiaryImportService mctsBeneficiaryImportService,long chunkSize) {
        this.recordList = recordList;
        this.mctsImport = mctsImport;
        this.importOrigin = importOrigin;
        this.locationFinder = locationFinder;
        this.mctsBeneficiaryValueProcessor = mctsBeneficiaryValueProcessor;
        this.mctsBeneficiaryImportService = mctsBeneficiaryImportService;
        this.chunkSize=chunkSize;
    }

    @Override
    public ThreadProcessorObject call() throws Exception { //NOPMD SignatureDeclareThrowsException

        ThreadProcessorObject threadProcessorObject = new ThreadProcessorObject();
        Map<String, Object> rejectedMothers = new HashMap<>();
        Map<String, Object> rejectionStatus = new HashMap<>();
        MotherImportRejection motherImportRejection;

        String id;
        String contactNumber;
        String motherInstance;
        if (mctsImport) {
            id = KilkariConstants.BENEFICIARY_ID;
            contactNumber = KilkariConstants.MSISDN;
            motherInstance = KilkariConstants.MCTS_MOTHER;
        } else {
            id = KilkariConstants.RCH_ID;
            contactNumber = KilkariConstants.MOBILE_NO;
            motherInstance = KilkariConstants.RCH_MOTHER;
        }
        int count = 0;
        Timer timer = new Timer("mom", "moms");

        for (Map<String, Object> record : recordList) {
            count++;
            LOGGER.debug("Started mother import for msisdn {} beneficiary_id {}", record.get(contactNumber), record.get(id));

            MctsMother mother = mctsImport ? mctsBeneficiaryValueProcessor.getOrCreateMotherInstance((String) record.get(id)) : mctsBeneficiaryValueProcessor.getOrCreateRchMotherInstance((String) record.get(id), (String) record.get(KilkariConstants.MCTS_ID));
            if (mother == null) {
                MotherImportRejection motherImportRejection1 = motherRejectionRch(convertMapToRchMother(record), false, RejectionReasons.DATA_INTEGRITY_ERROR.toString(), KilkariConstants.CREATE);
                rejectedMothers.put(motherImportRejection1.getIdNo(), motherImportRejection1);
                rejectionStatus.put(motherImportRejection1.getIdNo(), motherImportRejection1.getAccepted());
                LOGGER.error("RchId is empty while importing mother at msisdn {} beneficiary_id {}", record.get(contactNumber), record.get(id));
                continue;
            }

            String action = (mother.getId() == null) ? KilkariConstants.CREATE : KilkariConstants.UPDATE;
            record.put(KilkariConstants.ACTION, action);
            record.put(motherInstance, mother);

            try {
                LOGGER.debug("Calling Import Mother Record for"  + count );
                motherImportRejection = mctsBeneficiaryImportService.importMotherRecord(record, importOrigin, locationFinder,isCapacityExceededCheck);
                LOGGER.debug("Completed  Import Mother Record for" +  count);
                if (motherImportRejection != null) {
                    if (mctsImport) {
                        rejectedMothers.put(motherImportRejection.getIdNo(), motherImportRejection);
                        rejectionStatus.put(motherImportRejection.getIdNo(), motherImportRejection.getAccepted());
                    } else {
                        rejectedMothers.put(motherImportRejection.getRegistrationNo(), motherImportRejection);
                        rejectionStatus.put(motherImportRejection.getRegistrationNo(), motherImportRejection.getAccepted());
                    }
                }
                if (count % KilkariConstants.PROGRESS_INTERVAL == 0) {
                    LOGGER.debug(KilkariConstants.IMPORTED, timer.frequency(count));
                }

                // changes made for subscription capacity bug-fix open
                isCapacityExceededCheck=false;
                savedRecords++;
                if(savedRecords>=chunkSize){ // define the chunk
                    isCapacityExceededCheck=true;
                    savedRecords=0;
                }
                // subscription capacity bug-fix close


            } catch (RuntimeException e) {
                LOGGER.error("Mother import Error. Error while importing mother at msisdn {} beneficiary_id {}", record.get(contactNumber), record.get(id), e);
            }
        }
        threadProcessorObject.setRejectedBeneficiaries(rejectedMothers);
        threadProcessorObject.setRejectionStatus(rejectionStatus);
        threadProcessorObject.setRecordsProcessed(count);
        return threadProcessorObject;
    }
}