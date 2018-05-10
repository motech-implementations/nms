package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.kilkari.domain.DeactivatedBeneficiary;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionError;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionRejectionReason;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.exception.MultipleSubscriberException;
import org.motechproject.nms.kilkari.repository.DeactivatedBeneficiaryDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionErrorDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryValueProcessor;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.kilkari.utils.MctsBeneficiaryUtils;
import org.motechproject.nms.kilkari.domain.RejectionReasons;
import org.motechproject.nms.region.domain.LocationFinder;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.motechproject.nms.region.service.LocationService;
import org.motechproject.nms.kilkari.service.ActionFinderService;
import org.motechproject.nms.rejectionhandler.domain.ChildImportRejection;
import org.motechproject.nms.rejectionhandler.domain.MotherImportRejection;
import org.motechproject.nms.rejectionhandler.service.ChildRejectionService;
import org.motechproject.nms.rejectionhandler.service.MotherRejectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Map;

import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.childRejectionMcts;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.motherRejectionMcts;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.convertMapToChild;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.convertMapToMother;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.childRejectionRch;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.motherRejectionRch;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.convertMapToRchChild;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.convertMapToRchMother;

/**
 * Implementation of the {@link MctsBeneficiaryImportService} interface.
 */
@Service("mctsBeneficiaryImportService")
public class MctsBeneficiaryImportServiceImpl implements MctsBeneficiaryImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MctsBeneficiaryImportServiceImpl.class);
    private static final int RECORDS_PART_SIZE = 100;


    private SubscriptionService subscriptionService;
    private SubscriptionErrorDataService subscriptionErrorDataService;
    private LocationService locationService;
    private SubscriberService subscriberService;
    private MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor;
    private SubscriptionPack pregnancyPack;
    private SubscriptionPack childPack;
    private MctsMotherDataService mctsMotherDataService;
    private DeactivatedBeneficiaryDataService deactivatedBeneficiaryDataService;

    private static final Integer REJECTION_PART_SIZE = 10;

    @Autowired
    public MctsBeneficiaryImportServiceImpl(SubscriptionService subscriptionService,
                                            SubscriptionErrorDataService subscriptionErrorDataService,
                                            LocationService locationService, SubscriberService subscriberService,
                                            MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor,
                                            MctsMotherDataService mctsMotherDataService,
                                            DeactivatedBeneficiaryDataService deactivatedBeneficiaryDataService) {
        this.subscriptionService = subscriptionService;
        this.subscriptionErrorDataService = subscriptionErrorDataService;
        this.locationService = locationService;
        this.subscriberService = subscriberService;
        this.mctsBeneficiaryValueProcessor = mctsBeneficiaryValueProcessor;
        this.mctsMotherDataService = mctsMotherDataService;
        this.deactivatedBeneficiaryDataService = deactivatedBeneficiaryDataService;
    }

    @Autowired
    private MotherRejectionService motherRejectionService;

    @Autowired
    private ChildRejectionService childRejectionService;

    @Autowired
    private ActionFinderService actionFinderService;




    @Override // NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public boolean importMotherRecord(Map<String, Object> record, SubscriptionOrigin importOrigin) { //NOPMD NcssMethodCount
        if (pregnancyPack == null) {
            pregnancyPack = subscriptionService.getSubscriptionPack(SubscriptionPackType.PREGNANCY);
        }
        MctsMother mother;
        Long msisdn;
        Boolean abortion;
        Boolean stillBirth;
        LocalDate lastUpdatedDateNic;
        String beneficiaryId;
        String action = "";
        Boolean flagForMcts = true;
        if (importOrigin.equals(SubscriptionOrigin.MCTS_IMPORT)) {
            action = actionFinderService.motherActionFinder(convertMapToMother(record));
            beneficiaryId = (String) record.get(KilkariConstants.BENEFICIARY_ID);
            mother = mctsBeneficiaryValueProcessor.getOrCreateMotherInstance(beneficiaryId);
            msisdn = (Long) record.get(KilkariConstants.MSISDN);
            abortion = (Boolean) record.get(KilkariConstants.ABORTION);
            stillBirth = (Boolean) record.get(KilkariConstants.STILLBIRTH);
            lastUpdatedDateNic = (LocalDate) record.get(KilkariConstants.LAST_UPDATE_DATE);
        } else {
            flagForMcts = false;
            action = actionFinderService.rchMotherActionFinder((convertMapToRchMother(record)));
            beneficiaryId = (String) record.get(KilkariConstants.RCH_ID);
            String mctsId = (String) record.get(KilkariConstants.MCTS_ID);
            mother = mctsBeneficiaryValueProcessor.getOrCreateRchMotherInstance(beneficiaryId, mctsId);
            msisdn = (Long) record.get(KilkariConstants.MOBILE_NO);
            abortion = (Boolean) record.get(KilkariConstants.ABORTION_TYPE);
            stillBirth = (Boolean) record.get(KilkariConstants.DELIVERY_OUTCOMES);
            lastUpdatedDateNic = (LocalDate) record.get(KilkariConstants.EXECUTION_DATE);
        }

        String name = (String) record.get(KilkariConstants.BENEFICIARY_NAME);
        DateTime lmp = (DateTime) record.get(KilkariConstants.LMP);
        DateTime motherDOB = (DateTime) record.get(KilkariConstants.MOTHER_DOB);
        Boolean death = (Boolean) record.get(KilkariConstants.DEATH);

        if (mother == null) {
            return createUpdateMotherRejections(flagForMcts, record, action, RejectionReasons.DATA_INTEGRITY_ERROR, false);
        }

        boolean isInvalidLMP = (mother.getId() == null || (mother.getId() != null && mother.getLastMenstrualPeriod() == null)) && !validateReferenceDate(lmp, SubscriptionPackType.PREGNANCY, msisdn, beneficiaryId, importOrigin);

        if (isInvalidLMP) {
            return createUpdateMotherRejections(flagForMcts, record, action, RejectionReasons.INVALID_LMP_DATE, false);
        }

        // validate msisdn
        if (!validateMsisdn(msisdn)) {
            return createUpdateMotherRejections(flagForMcts, record, action, RejectionReasons.MOBILE_NUMBER_EMPTY_OR_WRONG_FORMAT, false);
        }

        // validate lmp date. We do not sanitize for lmp in the future to be in sync with MCTS data
        // NOTE: getId is a way to see if this is a new user. We only accept new users if they
        // have 12 weeks left in the pack. For existing users, their lmp could be updated to
        // an earlier date if it's an complete mother record(i.e not created through child import)
        // validate and set location
        try {
            MctsBeneficiaryUtils.setLocationFields(locationService.getLocations(record), mother);
        } catch (InvalidLocationException le) {
            LOGGER.error(le.toString());
            subscriptionErrorDataService.create(new SubscriptionError(msisdn, beneficiaryId, SubscriptionRejectionReason.INVALID_LOCATION,
                    SubscriptionPackType.PREGNANCY, le.getMessage(), importOrigin));
            return createUpdateMotherRejections(flagForMcts, record, action, RejectionReasons.INVALID_LOCATION, false);
        }

        //validate if it's an updated record compared to one from database
        if (mother.getUpdatedDateNic() != null && (lastUpdatedDateNic == null || mother.getUpdatedDateNic().isAfter(lastUpdatedDateNic))) {
            subscriptionErrorDataService.create(new SubscriptionError(msisdn, beneficiaryId,
                    SubscriptionRejectionReason.BENEFICIARY_ALREADY_SUBSCRIBED, SubscriptionPackType.PREGNANCY, "Updated Record exits", importOrigin));
            return createUpdateMotherRejections(flagForMcts, record, action, RejectionReasons.UPDATED_RECORD_ALREADY_EXISTS, false);
        }

        List<DeactivatedBeneficiary> deactivatedUsers = deactivatedBeneficiaryDataService.findByExternalId(beneficiaryId);
        if (deactivatedUsers != null && deactivatedUsers.size() > 0) {
            for (DeactivatedBeneficiary deactivatedUser : deactivatedUsers) {
                if (deactivatedUser.getOrigin() == importOrigin) {
                    String message = deactivatedUser.isCompletedSubscription() ? "Subscription completed" : "User deactivated";
                    if (message.length() > 2) {
                        subscriptionErrorDataService.create(new SubscriptionError(msisdn, beneficiaryId,
                                SubscriptionRejectionReason.BENEFICIARY_ALREADY_SUBSCRIBED, SubscriptionPackType.PREGNANCY, message, importOrigin));
                        return createUpdateMotherRejections(flagForMcts, record, action, RejectionReasons.UPDATED_RECORD_ALREADY_EXISTS, false);
                    }
                }
            }
        }

        // Check if existing subscription needs to be deactivated
        Boolean deactivate = ((abortion != null) && abortion) || ((stillBirth != null) && stillBirth) || ((death != null) && death);  // NO CHECKSTYLE Boolean Expression Complexity
        if (deactivate && (mother.getId() == null)) {
            return createUpdateMotherRejections(flagForMcts, record, action, RejectionReasons.ABORT_STILLBIRTH_DEATH, false);
        }

        mother.setName(name);
        mother.setDateOfBirth(motherDOB);
        mother.setUpdatedDateNic(lastUpdatedDateNic);

        Subscription subscription;
        if (importOrigin.equals(SubscriptionOrigin.MCTS_IMPORT)) {
            //validate if an ACTIVE child is already present for the mother. If yes, ignore the update
            if (childAlreadyPresent(beneficiaryId)) {
                subscriptionErrorDataService.create(new SubscriptionError(msisdn, beneficiaryId,
                        SubscriptionRejectionReason.ACTIVE_CHILD_PRESENT, SubscriptionPackType.PREGNANCY, "Active child is present for this mother.", importOrigin));
                return createUpdateMotherRejections(flagForMcts, record, action, RejectionReasons.ACTIVE_CHILD_PRESENT, false);
            }
            subscription = subscriberService.updateMotherSubscriber(msisdn, mother, lmp, record, action);
        } else {
            Long caseNo = (Long) record.get(KilkariConstants.CASE_NO);
            // validate caseNo
            if (!validateCaseNo(caseNo, mother)) {
                motherRejectionService.createOrUpdateMother(motherRejectionRch(convertMapToRchMother(record), false, RejectionReasons.INVALID_CASE_NO.toString(), action));
                return false;
            }
            subscription = subscriberService.updateRchMotherSubscriber(msisdn, mother, lmp, caseNo, deactivate, record, action);
        }
        // We rejected the update/create for the subscriber
        if (subscription == null) {
            return false;
        }

        if ((abortion != null) && abortion) {
            subscriptionService.deactivateSubscription(subscription, DeactivationReason.MISCARRIAGE_OR_ABORTION);
            return true;
        }

        if ((stillBirth != null) && stillBirth) {
            subscriptionService.deactivateSubscription(subscription, DeactivationReason.STILL_BIRTH);
            return true;
        }

        if ((death != null) && death) {
            subscriptionService.deactivateSubscription(subscription, DeactivationReason.MATERNAL_DEATH);
            return true;
        }

        return createUpdateMotherRejections(flagForMcts, record, action, null, true);
    }

    @Override // NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public MotherImportRejection importMotherRecordCSV(Map<String, Object> record, SubscriptionOrigin importOrigin, LocationFinder locationFinder) { //NOPMD NcssMethodCount
        if (pregnancyPack == null) {
            pregnancyPack = subscriptionService.getSubscriptionPack(SubscriptionPackType.PREGNANCY);
        }
        MctsMother mother;
        Long msisdn;
        Boolean abortion;
        Boolean stillBirth;
        LocalDate lastUpdatedDateNic;
        String beneficiaryId;
        String action = "";
        Boolean flagForMcts = true;
        if (importOrigin.equals(SubscriptionOrigin.MCTS_IMPORT)) {
            action = actionFinderService.motherActionFinder(convertMapToMother(record));
            beneficiaryId = (String) record.get(KilkariConstants.BENEFICIARY_ID);
            mother = mctsBeneficiaryValueProcessor.getOrCreateMotherInstance(beneficiaryId);
            msisdn = (Long) record.get(KilkariConstants.MSISDN);
            abortion = (Boolean) record.get(KilkariConstants.ABORTION);
            stillBirth = (Boolean) record.get(KilkariConstants.STILLBIRTH);
            lastUpdatedDateNic = (LocalDate) record.get(KilkariConstants.LAST_UPDATE_DATE);
        } else {
            flagForMcts = false;
            action = actionFinderService.rchMotherActionFinder((convertMapToRchMother(record)));
            beneficiaryId = (String) record.get(KilkariConstants.RCH_ID);
            String mctsId = (String) record.get(KilkariConstants.MCTS_ID);
            mother = mctsBeneficiaryValueProcessor.getOrCreateRchMotherInstance(beneficiaryId, mctsId);
            msisdn = (Long) record.get(KilkariConstants.MOBILE_NO);
            abortion = (Boolean) record.get(KilkariConstants.ABORTION_TYPE);
            stillBirth = (Boolean) record.get(KilkariConstants.DELIVERY_OUTCOMES);
            lastUpdatedDateNic = (LocalDate) record.get(KilkariConstants.EXECUTION_DATE);
        }

        String name = (String) record.get(KilkariConstants.BENEFICIARY_NAME);
        DateTime lmp = (DateTime) record.get(KilkariConstants.LMP);
        DateTime motherDOB = (DateTime) record.get(KilkariConstants.MOTHER_DOB);
        Boolean death = (Boolean) record.get(KilkariConstants.DEATH);

        if (mother == null) {
            return createUpdateMotherRejectionsCSV(flagForMcts, record, action, RejectionReasons.DATA_INTEGRITY_ERROR, false);
        }

        boolean isInvalidLMP = (mother.getId() == null || (mother.getId() != null && mother.getLastMenstrualPeriod() == null)) && !validateReferenceDate(lmp, SubscriptionPackType.PREGNANCY, msisdn, beneficiaryId, importOrigin);

        if (isInvalidLMP) {
            return createUpdateMotherRejectionsCSV(flagForMcts, record, action, RejectionReasons.INVALID_LMP_DATE, false);
        }

        // validate msisdn
        if (!validateMsisdn(msisdn)) {
            return createUpdateMotherRejectionsCSV(flagForMcts, record, action, RejectionReasons.MOBILE_NUMBER_EMPTY_OR_WRONG_FORMAT, false);
        }

        // validate lmp date. We do not sanitize for lmp in the future to be in sync with MCTS data
        // NOTE: getId is a way to see if this is a new user. We only accept new users if they
        // have 12 weeks left in the pack. For existing users, their lmp could be updated to
        // an earlier date if it's an complete mother record(i.e not created through child import)
        // validate and set location
        try {
            MctsBeneficiaryUtils.setLocationFieldsCSV(locationFinder, record, mother);
        } catch (InvalidLocationException le) {
            LOGGER.error(le.toString());
            subscriptionErrorDataService.create(new SubscriptionError(msisdn, beneficiaryId, SubscriptionRejectionReason.INVALID_LOCATION,
                    SubscriptionPackType.PREGNANCY, le.getMessage(), importOrigin));
            return createUpdateMotherRejectionsCSV(flagForMcts, record, action, RejectionReasons.INVALID_LOCATION, false);
        }

        //validate if it's an updated record compared to one from database
        if (mother.getUpdatedDateNic() != null && (lastUpdatedDateNic == null || mother.getUpdatedDateNic().isAfter(lastUpdatedDateNic))) {
            subscriptionErrorDataService.create(new SubscriptionError(msisdn, beneficiaryId,
                    SubscriptionRejectionReason.BENEFICIARY_ALREADY_SUBSCRIBED, SubscriptionPackType.PREGNANCY, "Updated Record exits", importOrigin));
            return createUpdateMotherRejectionsCSV(flagForMcts, record, action, RejectionReasons.UPDATED_RECORD_ALREADY_EXISTS, false);
        }

        List<DeactivatedBeneficiary> deactivatedUsers = deactivatedBeneficiaryDataService.findByExternalId(beneficiaryId);
        if (deactivatedUsers != null && deactivatedUsers.size() > 0) {
            for (DeactivatedBeneficiary deactivatedUser : deactivatedUsers) {
                if (deactivatedUser.getOrigin() == importOrigin) {
                    String message = deactivatedUser.isCompletedSubscription() ? "Subscription completed" : "User deactivated";
                    if (message.length() > 2) {
                        subscriptionErrorDataService.create(new SubscriptionError(msisdn, beneficiaryId,
                                SubscriptionRejectionReason.BENEFICIARY_ALREADY_SUBSCRIBED, SubscriptionPackType.PREGNANCY, message, importOrigin));
                        return createUpdateMotherRejectionsCSV(flagForMcts, record, action, RejectionReasons.UPDATED_RECORD_ALREADY_EXISTS, false);
                    }
                }
            }
        }

        // Check if existing subscription needs to be deactivated
        Boolean deactivate = ((abortion != null) && abortion) || ((stillBirth != null) && stillBirth) || ((death != null) && death);  // NO CHECKSTYLE Boolean Expression Complexity
        if (deactivate && (mother.getId() == null)) {
            return createUpdateMotherRejectionsCSV(flagForMcts, record, action, RejectionReasons.ABORT_STILLBIRTH_DEATH, false);
        }

        mother.setName(name);
        mother.setDateOfBirth(motherDOB);
        mother.setUpdatedDateNic(lastUpdatedDateNic);

        Subscription subscription;
        if (importOrigin.equals(SubscriptionOrigin.MCTS_IMPORT)) {
            //validate if an ACTIVE child is already present for the mother. If yes, ignore the update
            if (childAlreadyPresent(beneficiaryId)) {
                subscriptionErrorDataService.create(new SubscriptionError(msisdn, beneficiaryId,
                        SubscriptionRejectionReason.ACTIVE_CHILD_PRESENT, SubscriptionPackType.PREGNANCY, "Active child is present for this mother.", importOrigin));
                return createUpdateMotherRejectionsCSV(flagForMcts, record, action, RejectionReasons.ACTIVE_CHILD_PRESENT, false);
            }
            subscription = subscriberService.updateMotherSubscriber(msisdn, mother, lmp, record, action);
        } else {
            Long caseNo = (Long) record.get(KilkariConstants.CASE_NO);
            // validate caseNo
            if (!validateCaseNo(caseNo, mother)) {
                return motherRejectionRch(convertMapToRchMother(record), false, RejectionReasons.INVALID_CASE_NO.toString(), action);
            }
            subscription = subscriberService.updateRchMotherSubscriber(msisdn, mother, lmp, caseNo, deactivate, record, action);
        }
        // We rejected the update/create for the subscriber
//        if (subscription == null) {
//            return false;
//        }

        if ((abortion != null) && abortion) {
            subscriptionService.deactivateSubscription(subscription, DeactivationReason.MISCARRIAGE_OR_ABORTION);
        }

        if ((stillBirth != null) && stillBirth) {
            subscriptionService.deactivateSubscription(subscription, DeactivationReason.STILL_BIRTH);
        }

        if ((death != null) && death) {
            subscriptionService.deactivateSubscription(subscription, DeactivationReason.MATERNAL_DEATH);
        }

        return createUpdateMotherRejectionsCSV(flagForMcts, record, action, null, true);
    }

    @Override // NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public ChildImportRejection importChildRecord(Map<String, Object> record, SubscriptionOrigin importOrigin) { //NOPMD NcssMethodCount
        if (childPack == null) {
            childPack = subscriptionService.getSubscriptionPack(SubscriptionPackType.CHILD);
        }
        MctsChild child;
        Long msisdn;
        MctsMother mother;
        LocalDate lastUpdateDateNic;
        String childId;
        String action = "";
        Boolean flagForMcts = true;

        if (importOrigin.equals(SubscriptionOrigin.MCTS_IMPORT)) {
            action = actionFinderService.childActionFinder(convertMapToChild(record));
            childId = (String) record.get(KilkariConstants.BENEFICIARY_ID);
            child = mctsBeneficiaryValueProcessor.getOrCreateChildInstance(childId);
            msisdn = (Long) record.get(KilkariConstants.MSISDN);
            if (record.get(KilkariConstants.MOTHER_ID) != null) {
                Object motherRecord = record.get(KilkariConstants.MOTHER_ID);

                try {
                    MctsMother motherInstance = (MctsMother) motherRecord;
                    mother = mctsBeneficiaryValueProcessor.getMotherInstanceByBeneficiaryId(motherInstance.getBeneficiaryId());
                } catch (Exception e) {
                    mother = mctsBeneficiaryValueProcessor.getMotherInstanceByBeneficiaryId(motherRecord.toString());
                }

            } else {
                mother = null;
            }
            lastUpdateDateNic = (LocalDate) record.get(KilkariConstants.LAST_UPDATE_DATE);
        } else {
            flagForMcts = false;
            action = (String) record.get(KilkariConstants.ACTION);
            childId = (String) record.get(KilkariConstants.RCH_ID);
            child = (MctsChild) record.get(KilkariConstants.RCH_CHILD);
            msisdn = (Long) record.get(KilkariConstants.MOBILE_NO);
            lastUpdateDateNic = (LocalDate) record.get(KilkariConstants.EXECUTION_DATE);
            if (record.get(KilkariConstants.RCH_MOTHER_ID) != null || record.get(KilkariConstants.MCTS_MOTHER_ID) != null) {
                String motherRchId = record.get(KilkariConstants.RCH_MOTHER_ID) == null || "".equals(record.get(KilkariConstants.RCH_MOTHER_ID)) || "0".equalsIgnoreCase(record.get(KilkariConstants.RCH_MOTHER_ID).toString()) ? null : record.get(KilkariConstants.RCH_MOTHER_ID).toString();
                String motherMctsId = record.get(KilkariConstants.MCTS_MOTHER_ID) == null || "".equals(record.get(KilkariConstants.MCTS_MOTHER_ID)) || "0".equalsIgnoreCase(record.get(KilkariConstants.MCTS_MOTHER_ID).toString()) ? null : record.get(KilkariConstants.MCTS_MOTHER_ID).toString();
                if (motherRchId == null && motherMctsId != null) {
                    mother = mctsBeneficiaryValueProcessor.getMotherInstanceByBeneficiaryId(motherMctsId);
                } else if (motherRchId != null) {
                    mother = mctsBeneficiaryValueProcessor.getOrCreateRchMotherInstance(motherRchId, motherMctsId);
                } else {
                    mother = null;
                }
            } else {
                mother = null;
            }
        }
        String name = (String) record.get(KilkariConstants.BENEFICIARY_NAME);
        DateTime dob = (DateTime) record.get(KilkariConstants.DOB);
        Boolean death = (Boolean) record.get(KilkariConstants.DEATH);

        if ((child == null)) {
            return createUpdateChildRejections(flagForMcts, record, action, RejectionReasons.DATA_INTEGRITY_ERROR, false);
        }

        boolean isInValidDOB = child.getId() == null && !validateReferenceDate(dob, SubscriptionPackType.CHILD, msisdn, childId, importOrigin);
        if (isInValidDOB) {
            return createUpdateChildRejections(flagForMcts, record, action, RejectionReasons.INVALID_DOB, false);
        }

        //validate mother
        if (!validateMother(mother, child)) {
            return createUpdateChildRejections(flagForMcts, record, action, RejectionReasons.ALREADY_LINKED_WITH_A_DIFFERENT_MOTHER_ID, false);
        }

        // validate msisdn
        if (!validateMsisdn(msisdn)) {
            return createUpdateChildRejections(flagForMcts, record, action, RejectionReasons.MOBILE_NUMBER_EMPTY_OR_WRONG_FORMAT, false);
        }

        // validate dob. We do not sanitize for dob in the future to be in sync with MCTS data
        // NOTE: getId is a way to check for new user. We only accept new children if they have 12 weeks left
        // in the pack. Existing children could have their dob updated to an earlier date

        // validate and set location
        try {
            MctsBeneficiaryUtils.setLocationFields(locationService.getLocations(record), child);
            if (mother != null) {
                MctsBeneficiaryUtils.setLocationFields(locationService.getLocations(record), mother);
            }
        } catch (InvalidLocationException le) {
            LOGGER.error(le.toString());
            return createUpdateChildRejections(flagForMcts, record, action, RejectionReasons.INVALID_LOCATION, false);
        }

        //validate if it's an updated record compared to one from database
        if (child.getUpdatedDateNic() != null && (lastUpdateDateNic == null || child.getUpdatedDateNic().isAfter(lastUpdateDateNic))) {
            return createUpdateChildRejections(flagForMcts, record, action, RejectionReasons.UPDATED_RECORD_ALREADY_EXISTS, false);
        }

        List<DeactivatedBeneficiary> deactivatedUsers = deactivatedBeneficiaryDataService.findByExternalId(childId);
        if (deactivatedUsers != null && deactivatedUsers.size() > 0) {
            for (DeactivatedBeneficiary deactivatedUser : deactivatedUsers) {
                if (deactivatedUser.getOrigin() == importOrigin) {
                    String message = deactivatedUser.isCompletedSubscription() ? "Subscription completed" : "User deactivated";
                    if (message.length() > 2) {
                        return createUpdateChildRejections(flagForMcts, record, action, RejectionReasons.UPDATED_RECORD_ALREADY_EXISTS, false);
                    }
                }
            }
        }
        //1.0.34 Bug child.getId() == null was not checked
        if ((death != null) && death && (child.getId() == null)) {
            return createUpdateChildRejections(flagForMcts, record, action, RejectionReasons.CHILD_DEATH, false);
        }

        child.setName(name);
        child.setMother(mother);
        child.setUpdatedDateNic(lastUpdateDateNic);

        if (importOrigin.equals(SubscriptionOrigin.MCTS_IMPORT)) {
            return subscriberService.updateChildSubscriber(msisdn, child, dob, record, action);
        } else {
            return subscriberService.updateRchChildSubscriber(msisdn, child, dob, record, action);

        }
    }

    @Override // NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public ChildImportRejection importChildRecordCSV(Map<String, Object> record, SubscriptionOrigin importOrigin, LocationFinder locationFinder) { //NOPMD NcssMethodCount
        if (childPack == null) {
            childPack = subscriptionService.getSubscriptionPack(SubscriptionPackType.CHILD);
        }
        MctsChild child;
        Long msisdn;
        MctsMother mother;
        LocalDate lastUpdateDateNic;
        String childId;
        String action = "";
        Boolean flagForMcts = true;

        if (importOrigin.equals(SubscriptionOrigin.MCTS_IMPORT)) {
            action = actionFinderService.childActionFinder(convertMapToChild(record));
            childId = (String) record.get(KilkariConstants.BENEFICIARY_ID);
            child = mctsBeneficiaryValueProcessor.getOrCreateChildInstance(childId);
            msisdn = (Long) record.get(KilkariConstants.MSISDN);
            if (record.get(KilkariConstants.MOTHER_ID) != null) {
                Object motherRecord = record.get(KilkariConstants.MOTHER_ID);

                try {
                    MctsMother motherInstance = (MctsMother) motherRecord;
                    mother = mctsBeneficiaryValueProcessor.getMotherInstanceByBeneficiaryId(motherInstance.getBeneficiaryId());
                } catch (Exception e) {
                    mother = mctsBeneficiaryValueProcessor.getMotherInstanceByBeneficiaryId(motherRecord.toString());
                }

            } else {
                mother = null;
            }
            lastUpdateDateNic = (LocalDate) record.get(KilkariConstants.LAST_UPDATE_DATE);
        } else {
            flagForMcts = false;
            action = (String) record.get(KilkariConstants.ACTION);
            childId = (String) record.get(KilkariConstants.RCH_ID);
            child = (MctsChild) record.get(KilkariConstants.RCH_CHILD);
            msisdn = (Long) record.get(KilkariConstants.MOBILE_NO);
            lastUpdateDateNic = (LocalDate) record.get(KilkariConstants.EXECUTION_DATE);
            if (record.get(KilkariConstants.RCH_MOTHER_ID) != null || record.get(KilkariConstants.MCTS_MOTHER_ID) != null) {
                String motherRchId = record.get(KilkariConstants.RCH_MOTHER_ID) == null || "".equals(record.get(KilkariConstants.RCH_MOTHER_ID)) || "0".equalsIgnoreCase(record.get(KilkariConstants.RCH_MOTHER_ID).toString()) ? null : record.get(KilkariConstants.RCH_MOTHER_ID).toString();
                String motherMctsId = record.get(KilkariConstants.MCTS_MOTHER_ID) == null || "".equals(record.get(KilkariConstants.MCTS_MOTHER_ID)) || "0".equalsIgnoreCase(record.get(KilkariConstants.MCTS_MOTHER_ID).toString()) ? null : record.get(KilkariConstants.MCTS_MOTHER_ID).toString();
                if (motherRchId == null && motherMctsId != null) {
                    mother = mctsBeneficiaryValueProcessor.getMotherInstanceByBeneficiaryId(motherMctsId);
                } else if (motherRchId != null) {
                    mother = mctsBeneficiaryValueProcessor.getOrCreateRchMotherInstance(motherRchId, motherMctsId);
                } else {
                    mother = null;
                }
            } else {
                mother = null;
            }
        }
        String name = (String) record.get(KilkariConstants.BENEFICIARY_NAME);
        DateTime dob = (DateTime) record.get(KilkariConstants.DOB);
        Boolean death = (Boolean) record.get(KilkariConstants.DEATH);

        if ((child == null)) {
            return createUpdateChildRejections(flagForMcts, record, action, RejectionReasons.DATA_INTEGRITY_ERROR, false);
        }

        boolean isInValidDOB = child.getId() == null && !validateReferenceDate(dob, SubscriptionPackType.CHILD, msisdn, childId, importOrigin);
        if (isInValidDOB) {
            return createUpdateChildRejections(flagForMcts, record, action, RejectionReasons.INVALID_DOB, false);
        }

        //validate mother
        if (!validateMother(mother, child)) {
            return createUpdateChildRejections(flagForMcts, record, action, RejectionReasons.ALREADY_LINKED_WITH_A_DIFFERENT_MOTHER_ID, false);
        }

        // validate msisdn
        if (!validateMsisdn(msisdn)) {
            return createUpdateChildRejections(flagForMcts, record, action, RejectionReasons.MOBILE_NUMBER_EMPTY_OR_WRONG_FORMAT, false);
        }

        // validate dob. We do not sanitize for dob in the future to be in sync with MCTS data
        // NOTE: getId is a way to check for new user. We only accept new children if they have 12 weeks left
        // in the pack. Existing children could have their dob updated to an earlier date

        // validate and set location
        try {
            MctsBeneficiaryUtils.setLocationFieldsCSV(locationFinder, record, child);
            if (mother != null) {
                MctsBeneficiaryUtils.setLocationFieldsCSV(locationFinder, record, mother);
            }
        } catch (InvalidLocationException le) {
            LOGGER.error(le.toString());
            return createUpdateChildRejections(flagForMcts, record, action, RejectionReasons.INVALID_LOCATION, false);
        }

        //validate if it's an updated record compared to one from database
        if (child.getUpdatedDateNic() != null && (lastUpdateDateNic == null || child.getUpdatedDateNic().isAfter(lastUpdateDateNic))) {
            return createUpdateChildRejections(flagForMcts, record, action, RejectionReasons.UPDATED_RECORD_ALREADY_EXISTS, false);
        }

        List<DeactivatedBeneficiary> deactivatedUsers = deactivatedBeneficiaryDataService.findByExternalId(childId);
        if (deactivatedUsers != null && deactivatedUsers.size() > 0) {
            for (DeactivatedBeneficiary deactivatedUser : deactivatedUsers) {
                if (deactivatedUser.getOrigin() == importOrigin) {
                    String message = deactivatedUser.isCompletedSubscription() ? "Subscription completed" : "User deactivated";
                    if (message.length() > 2) {
                        return createUpdateChildRejections(flagForMcts, record, action, RejectionReasons.UPDATED_RECORD_ALREADY_EXISTS, false);
                    }
                }
            }
        }
        //1.0.34 Bug child.getId() == null was not checked
        if ((death != null) && death && (child.getId() == null)) {
            return createUpdateChildRejections(flagForMcts, record, action, RejectionReasons.CHILD_DEATH, false);
        }

        child.setName(name);
        child.setMother(mother);
        child.setUpdatedDateNic(lastUpdateDateNic);

        if (importOrigin.equals(SubscriptionOrigin.MCTS_IMPORT)) {
            return subscriberService.updateChildSubscriber(msisdn, child, dob, record, action);
        } else {
            return subscriberService.updateRchChildSubscriber(msisdn, child, dob, record, action);

        }
    }

    // Create rejection records for all the invalid mothers
    private boolean createUpdateMotherRejections(Boolean isMcts, Map<String, Object> record, String action, RejectionReasons rejectionReason, Boolean accepted) {
        String rejectReason = rejectionReason == null ? null : rejectionReason.toString();
        if (isMcts) {
            motherRejectionService.createOrUpdateMother(motherRejectionMcts(convertMapToMother(record), accepted, rejectReason, action));

        } else {
            motherRejectionService.createOrUpdateMother(motherRejectionRch(convertMapToRchMother(record), accepted, rejectReason, action));

        }
        return accepted;
    }

    private MotherImportRejection createUpdateMotherRejectionsCSV(Boolean isMcts, Map<String, Object> record, String action, RejectionReasons rejectionReason, Boolean accepted) {
        String rejectReason = rejectionReason == null ? null : rejectionReason.toString();
        if (isMcts) {
            return motherRejectionMcts(convertMapToMother(record), accepted, rejectReason, action);

        } else {
            return motherRejectionRch(convertMapToRchMother(record), accepted, rejectReason, action);

        }
    }

    // Create rejection records for all the invalid childs
    private ChildImportRejection createUpdateChildRejections(Boolean isMcts, Map<String, Object> record, String action, RejectionReasons rejectionReason, Boolean accepted) {
        String rejectReason  = rejectionReason == null ? null : rejectionReason.toString();
        if (isMcts) {
            return childRejectionMcts(convertMapToChild(record), accepted, rejectReason, action);
        } else {
            return childRejectionRch(convertMapToRchChild(record), accepted, rejectReason, action);
        }
    }

    @Override
    public List<List<Map<String, Object>>> splitRecords(List<Map<String, Object>> recordList, String contactNumber) {
        List<List<Map<String, Object>>> recordListArray = new ArrayList<>();
        int count = 0;
        while(count < recordList.size()) {
            List<Map<String, Object>> recordListPart = new ArrayList<>();
            while(recordListPart.size() < RECORDS_PART_SIZE && count < recordList.size()) {
                recordListPart.add(recordList.get(count));
                count++;
            }
            //Add all records with same contact number to the same part
            while(count < recordList.size() && (recordList.get(count).get(contactNumber) == null? "0": recordList.get(count).get(contactNumber))
                    .equals(recordListPart.get(recordListPart.size()-1)
                            .get(contactNumber))){
                recordListPart.add(recordList.get(count));
                count++;
            }
            LOGGER.debug("Added records to part {}",recordListArray.size()+1);
            recordListArray.add(recordListPart);
            LOGGER.debug("Added recordListPart to recordListArray");
        }
        LOGGER.debug("Split {} records to {} parts", recordList.size(), recordListArray.size());
        return recordListArray;
    }

    private boolean validateMother(MctsMother mother, MctsChild child) {
        if (mother != null && child.getMother() != null && !mother.equals(child.getMother())) {
            return false;
        }
        return true;
    }

    private boolean validateMsisdn(Long msisdn) {
        if (msisdn == null || (msisdn.toString().length() != KilkariConstants.MSISDN_LENGTH)) {
            return false;
        }

        return true;
    }

    private boolean validateCaseNo(Long caseNo, MctsMother mother) {
        if (caseNo == null || (caseNo <= 0)) {
            return false;
        }

        if (mother.getMaxCaseNo() != null && caseNo < mother.getMaxCaseNo()) {
            return false;
        }

        return true;
    }

    private boolean childAlreadyPresent(final String motherBenificiaryId) {
        //Found mother by beneficiary id. If there is no mother already present,then import will
        //go to the next check. Else we get the subscriber by the mother id
        //and check if the child subscription is ACTIVE. If yes we do not update the mother.
        MctsMother mctsMother = null;

        try {
            mctsMother = mctsMotherDataService.findByBeneficiaryId(motherBenificiaryId);

            if (mctsMother == null) {
                return false;
            } else {
                Long motherId = mctsMother.getId();
                Subscriber subscriber = subscriberService.getSubscriberByMother(motherId);
                if (subscriber == null) {
                    return false;
                } else {
                    for (Subscription subscription : subscriber.getAllSubscriptions()) {
                        if (subscription.getSubscriptionPack().getType().equals(SubscriptionPackType.CHILD)
                                && subscription.getStatus().equals(SubscriptionStatus.ACTIVE)
                                && subscriber.getChild().getMother() != null
                                && subscriber.getChild().getMother().getBeneficiaryId().equals(motherBenificiaryId)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (MultipleSubscriberException m) {
            LOGGER.error(m.toString());
            return true;
        }
    }

    @Override
    public boolean validateReferenceDate(DateTime referenceDate, SubscriptionPackType packType, Long msisdn, String beneficiaryId, SubscriptionOrigin importOrigin) {
        if (pregnancyPack == null) {
            pregnancyPack = subscriptionService.getSubscriptionPack(SubscriptionPackType.PREGNANCY);
        }
        if (childPack == null) {
            childPack = subscriptionService.getSubscriptionPack(SubscriptionPackType.CHILD);
        }
        if (referenceDate == null) {
            subscriptionErrorDataService.create(
                    new SubscriptionError(msisdn, beneficiaryId,
                            (packType == SubscriptionPackType.PREGNANCY) ?
                                    SubscriptionRejectionReason.MISSING_LMP :
                                    SubscriptionRejectionReason.MISSING_DOB,
                            packType, "", importOrigin));
            return false;
        }

        if (packType == SubscriptionPackType.PREGNANCY) {
            String referenceDateValidationError = pregnancyPack.isReferenceDateValidForPack(referenceDate);
            if (!referenceDateValidationError.isEmpty()) {
                return false;
            }
        } else { // childPack
            LOGGER.debug("bdate: {}", referenceDate);
            String referenceDateValidationError = childPack.isReferenceDateValidForPack(referenceDate);
            if (!referenceDateValidationError.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    @Transactional
    public void createOrUpdateRchChildRejections(Map<String, Object> rejectedRecords, Map<String, Object> rejectionStatus) {

        // get rch Ids of all the rejected records
        Set<String> rchIds = rejectedRecords.keySet();
        List<ChildImportRejection> updateObjects = new ArrayList<>();
        List<ChildImportRejection> createObjects = new ArrayList<>();

        Map<String, Object> childRejects = childRejectionService.findChildRejectionByRchId(rchIds);
        ChildImportRejection child;
        for (String rchId : rchIds) {
            child = (ChildImportRejection) rejectedRecords.get(rchId);
            if (childRejects.get(rchId) != null) {
                ChildImportRejection dbChild = (ChildImportRejection) childRejects.get(rchId);
                try {
                    Method method = dbChild.getClass().getMethod("getCreationDate");  // Get creationDate from db object and set it in the update object
                    DateTime dateTime = (DateTime) method.invoke(dbChild);

                    method = child.getClass().getMethod("setCreationDate", DateTime.class);
                    method.invoke(child, dateTime);

                    method = dbChild.getClass().getMethod("getId");
                    Long id = (Long) method.invoke(dbChild);
                    method = child.getClass().getMethod("setId", Long.class);
                    method.invoke(child, id);
                } catch (IllegalAccessException|SecurityException|IllegalArgumentException|NoSuchMethodException|
                        InvocationTargetException e) {
                    LOGGER.error("Ignoring creation date and setting as now");

                }
                updateObjects.add(child);
                continue;
            }
            if (!(Boolean) rejectionStatus.get(rchId)) {
                createObjects.add(child);
            }
        }

        Long createdNo = (createObjects.size() == 0) ? 0 : rchChildBulkInsert(createObjects);
        Long updatedNo = (updateObjects.size() == 0) ? 0 : rchChildBulkUpdate(updateObjects);
        LOGGER.debug("Inserted {} and updated {} rejection records into database", createdNo, updatedNo);
    }

    private Long rchChildBulkInsert(List<ChildImportRejection> createObjects) {
        int count = 0;
        Long sqlCount = 0L;
        while(count < createObjects.size()) {
            List<ChildImportRejection> createObjectsPart = new ArrayList<>();
            while(createObjectsPart.size() < REJECTION_PART_SIZE && count<createObjects.size()) {
                createObjectsPart.add(createObjects.get(count));
                count++;
            }
            sqlCount += childRejectionService.rchBulkInsert(createObjectsPart);
            createObjectsPart.clear();
        }
        return sqlCount;
    }

    private Long rchChildBulkUpdate(List<ChildImportRejection> updateObjects) {
        int count = 0;
        Long sqlCount = 0L;
        while(count < updateObjects.size()) {
            List<ChildImportRejection> updateObjectsPart = new ArrayList<>();
            while(updateObjectsPart.size() < REJECTION_PART_SIZE && count<updateObjects.size()) {
                updateObjectsPart.add(updateObjects.get(count));
                count++;
            }
            sqlCount += childRejectionService.rchBulkUpdate(updateObjectsPart);
            updateObjectsPart.clear();
        }
        return sqlCount;
    }

    @Override
    @Transactional
    public void createOrUpdateMctsChildRejections(Map<String, Object> rejectedRecords, Map<String, Object> rejectionStatus) {

        // get rch Ids of all the rejected records
        Set<String> mctsIds = rejectedRecords.keySet();
        List<ChildImportRejection> updateObjects = new ArrayList<>();;
        List<ChildImportRejection> createObjects = new ArrayList<>();;

        Map<String, Object> childRejects = childRejectionService.findChildRejectionByMctsId(mctsIds);
        ChildImportRejection child;
        for (String mctsId : mctsIds) {
            child = (ChildImportRejection) rejectedRecords.get(mctsId);
            if (childRejects.get(mctsId) != null) {
                ChildImportRejection dbChild = (ChildImportRejection) childRejects.get(mctsId);
                try {
                    Method method = dbChild.getClass().getMethod("getCreationDate");  // Get creationDate from db object and set it in the update object
                    DateTime dateTime = (DateTime) method.invoke(dbChild);

                    method = child.getClass().getMethod("setCreationDate", DateTime.class);
                    method.invoke(child, dateTime);

                    method = dbChild.getClass().getMethod("getId");
                    Long id = (Long) method.invoke(dbChild);
                    method = child.getClass().getMethod("setId", Long.class);
                    method.invoke(child, id);
                } catch (IllegalAccessException|SecurityException|IllegalArgumentException|NoSuchMethodException|
                        InvocationTargetException e) {
                    LOGGER.error("Ignoring creation date and setting as now");

                }
                updateObjects.add(child);
                continue;
            }
            if (!(Boolean) rejectionStatus.get(mctsId)) {
                createObjects.add(child);
            }
        }

        Long createdNo = (createObjects.size() == 0) ? 0 : mctsChildBulkInsert(createObjects);
        Long updatedNo = (updateObjects.size() == 0) ? 0 : mctsChildBulkUpdate(updateObjects);
        LOGGER.debug("Inserted {} and updated {} rejection records into database", createdNo, updatedNo);
    }


    private Long mctsChildBulkInsert(List<ChildImportRejection> createObjects) {
        int count = 0;
        Long sqlCount = 0L;
        while(count < createObjects.size()) {
            List<ChildImportRejection> createObjectsPart = new ArrayList<>();
            while(createObjectsPart.size() < REJECTION_PART_SIZE && count<createObjects.size()) {
                createObjectsPart.add(createObjects.get(count));
                count++;
            }
            sqlCount += childRejectionService.mctsBulkInsert(createObjectsPart);
            createObjectsPart.clear();
        }
        return sqlCount;
    }

    private Long mctsChildBulkUpdate(List<ChildImportRejection> updateObjects) {
        int count = 0;
        Long sqlCount = 0L;
        while(count < updateObjects.size()) {
            List<ChildImportRejection> updateObjectsPart = new ArrayList<>();
            while(updateObjectsPart.size() < REJECTION_PART_SIZE && count<updateObjects.size()) {
                updateObjectsPart.add(updateObjects.get(count));
                count++;
            }
            sqlCount += childRejectionService.mctsBulkUpdate(updateObjectsPart);
            updateObjectsPart.clear();
        }
        return sqlCount;
    }


    @Override
    @Transactional
    public void createOrUpdateRchMotherRejections(Map<String, Object> rejectedRecords, Map<String, Object> rejectionStatus) {

        // get rch Ids of all the rejected records
        Set<String> rchIds = rejectedRecords.keySet();
        List<MotherImportRejection> updateObjects = new ArrayList<>();
        List<MotherImportRejection> createObjects = new ArrayList<>();

        Map<String, Object> motherRejects = motherRejectionService.findMotherRejectionByRchId(rchIds);
        MotherImportRejection mother;
        for (String rchId : rchIds) {
            mother = (MotherImportRejection) rejectedRecords.get(rchId);
            if (motherRejects.get(rchId) != null) {
                MotherImportRejection dbMother = (MotherImportRejection) motherRejects.get(rchId);
                try {
                    Method method = dbMother.getClass().getMethod("getCreationDate");  // Get creationDate from db object and set it in the update object
                    DateTime dateTime = (DateTime) method.invoke(dbMother);

                    method = mother.getClass().getMethod("setCreationDate", DateTime.class);
                    method.invoke(mother, dateTime);

                    method = dbMother.getClass().getMethod("getId");
                    Long id = (Long) method.invoke(dbMother);
                    method = mother.getClass().getMethod("setId", Long.class);
                    method.invoke(mother, id);
                } catch (IllegalAccessException|SecurityException|IllegalArgumentException|NoSuchMethodException|
                        InvocationTargetException e) {
                    LOGGER.error("Ignoring creation date and setting as now");

                }
                updateObjects.add(mother);
                continue;
            }
            if (!(Boolean) rejectionStatus.get(rchId)) {
                createObjects.add(mother);
            }
        }

        Long createdNo = (createObjects.size() == 0) ? 0 : rchMotherBulkInsert(createObjects);
        Long updatedNo = (updateObjects.size() == 0) ? 0 : rchMotherBulkUpdate(updateObjects);
        LOGGER.debug("Inserted {} and updated {} rejection records into database", createdNo, updatedNo);
    }

    private Long rchMotherBulkInsert(List<MotherImportRejection> createObjects) {
        int count = 0;
        Long sqlCount = 0L;
        while(count < createObjects.size()) {
            List<MotherImportRejection> createObjectsPart = new ArrayList<>();
            while(createObjectsPart.size() < REJECTION_PART_SIZE && count<createObjects.size()) {
                createObjectsPart.add(createObjects.get(count));
                count++;
            }
            sqlCount += motherRejectionService.rchBulkInsert(createObjectsPart);
            createObjectsPart.clear();
        }
        return sqlCount;
    }

    private Long rchMotherBulkUpdate(List<MotherImportRejection> updateObjects) {
        int count = 0;
        Long sqlCount = 0L;
        while(count < updateObjects.size()) {
            List<MotherImportRejection> updateObjectsPart = new ArrayList<>();
            while(updateObjectsPart.size() < REJECTION_PART_SIZE && count<updateObjects.size()) {
                updateObjectsPart.add(updateObjects.get(count));
                count++;
            }
            sqlCount += motherRejectionService.rchBulkUpdate(updateObjectsPart);
            updateObjectsPart.clear();
        }
        return sqlCount;
    }

    @Override
    @Transactional
    public void createOrUpdateMctsMotherRejections(Map<String, Object> rejectedRecords, Map<String, Object> rejectionStatus) {

        // get rch Ids of all the rejected records
        Set<String> mctsIds = rejectedRecords.keySet();
        List<MotherImportRejection> updateObjects = new ArrayList<>();;
        List<MotherImportRejection> createObjects = new ArrayList<>();;

        Map<String, Object> motherRejects = motherRejectionService.findMotherRejectionByMctsId(mctsIds);
        MotherImportRejection mother;
        for (String mctsId : mctsIds) {
            mother = (MotherImportRejection) rejectedRecords.get(mctsId);
            if (motherRejects.get(mctsId) != null) {
                MotherImportRejection dbMother = (MotherImportRejection) motherRejects.get(mctsId);
                try {
                    Method method = dbMother.getClass().getMethod("getCreationDate");  // Get creationDate from db object and set it in the update object
                    DateTime dateTime = (DateTime) method.invoke(dbMother);

                    method = mother.getClass().getMethod("setCreationDate", DateTime.class);
                    method.invoke(mother, dateTime);

                    method = dbMother.getClass().getMethod("getId");
                    Long id = (Long) method.invoke(dbMother);
                    method = mother.getClass().getMethod("setId", Long.class);
                    method.invoke(mother, id);
                } catch (IllegalAccessException|SecurityException|IllegalArgumentException|NoSuchMethodException|
                        InvocationTargetException e) {
                    LOGGER.error("Ignoring creation date and setting as now");

                }
                updateObjects.add(mother);
                continue;
            }
            if (!(Boolean) rejectionStatus.get(mctsId)) {
                createObjects.add(mother);
            }
        }

        Long createdNo = (createObjects.size() == 0) ? 0 : mctsMotherBulkInsert(createObjects);
        Long updatedNo = (updateObjects.size() == 0) ? 0 : mctsMotherBulkUpdate(updateObjects);
        LOGGER.debug("Inserted {} and updated {} rejection records into database", createdNo, updatedNo);
    }

    private Long mctsMotherBulkInsert(List<MotherImportRejection> createObjects) {
        int count = 0;
        Long sqlCount = 0L;
        while(count < createObjects.size()) {
            List<MotherImportRejection> createObjectsPart = new ArrayList<>();
            while(createObjectsPart.size() < REJECTION_PART_SIZE && count<createObjects.size()) {
                createObjectsPart.add(createObjects.get(count));
                count++;
            }
            sqlCount += motherRejectionService.mctsBulkInsert(createObjectsPart);
            createObjectsPart.clear();
        }
        return sqlCount;
    }

    private Long mctsMotherBulkUpdate(List<MotherImportRejection> updateObjects) {
        int count = 0;
        Long sqlCount = 0L;
        while(count < updateObjects.size()) {
            List<MotherImportRejection> updateObjectsPart = new ArrayList<>();
            while(updateObjectsPart.size() < REJECTION_PART_SIZE && count<updateObjects.size()) {
                updateObjectsPart.add(updateObjects.get(count));
                count++;
            }
            sqlCount += motherRejectionService.mctsBulkUpdate(updateObjectsPart);
            updateObjectsPart.clear();
        }
        return sqlCount;
    }


    @Override
    public Map<String, CellProcessor> getMotherProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        MctsBeneficiaryUtils.getBeneficiaryLocationMapping(mapping);
        getMotherMapping(mapping);

        mapping.put(KilkariConstants.BENEFICIARY_ID, new Optional(new GetString()));
        mapping.put(KilkariConstants.MSISDN, new Optional(new GetInstanceByString<Long>() {
            @Override
            public Long retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getMsisdnByString(value);
            }
        }));
        mapping.put(KilkariConstants.ABORTION, new Optional(new GetInstanceByString<Boolean>() {
            @Override
            public Boolean retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getAbortionDataFromString(value);
            }
        }));
        mapping.put(KilkariConstants.STILLBIRTH, new Optional(new GetInstanceByString<Boolean>() {
            @Override
            public Boolean retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getStillBirthFromString(value);
            }
        }));
        mapping.put(KilkariConstants.LAST_UPDATE_DATE, new Optional(new GetInstanceByString<LocalDate>() {
            @Override
            public LocalDate retrieve(String value) {
                return (LocalDate) mctsBeneficiaryValueProcessor.getDateByString(value).toLocalDate();
            }
        }));
        return mapping;
    }

    private void getMotherMapping(Map<String, CellProcessor> mapping) {

        mapping.put(KilkariConstants.BENEFICIARY_NAME, new GetString());
        mapping.put(KilkariConstants.LMP, new Optional(new GetInstanceByString<DateTime>() {
            @Override
            public DateTime retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getDateByString(value);
            }
        }));
        mapping.put(KilkariConstants.MOTHER_DOB, new Optional(new GetInstanceByString<DateTime>() {
            @Override
            public DateTime retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getDateByString(value);
            }
        }));
        mapping.put(KilkariConstants.DEATH, new Optional(new GetInstanceByString<Boolean>() {
            @Override
            public Boolean retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getDeathFromString(value);
            }
        }));
    }

    @Override
    public Map<String, CellProcessor> getRchMotherProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        MctsBeneficiaryUtils.getBeneficiaryLocationMapping(mapping);
        getMotherMapping(mapping);

        mapping.put(KilkariConstants.RCH_ID, new Optional(new GetString()));
        mapping.put(KilkariConstants.MCTS_ID, new Optional(new GetString()));
        mapping.put(KilkariConstants.MOBILE_NO, new Optional(new GetInstanceByString<Long>() {
            @Override
            public Long retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getMsisdnByString(value);
            }
        }));
        mapping.put(KilkariConstants.ABORTION_TYPE, new Optional(new GetInstanceByString<Boolean>() {
            @Override
            public Boolean retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getAbortionDataFromString(value);
            }
        }));
        mapping.put(KilkariConstants.DELIVERY_OUTCOMES, new Optional(new GetInstanceByString<Boolean>() {
            @Override
            public Boolean retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getStillBirthFromString(value);
            }
        }));
        mapping.put(KilkariConstants.EXECUTION_DATE, new Optional(new GetInstanceByString<LocalDate>() {
            @Override
            public LocalDate retrieve(String value) {
                return (LocalDate) mctsBeneficiaryValueProcessor.getDateByString(value).toLocalDate();
            }
        }));
        mapping.put(KilkariConstants.CASE_NO, new Optional(new GetInstanceByString<Long>() {
            @Override
            public Long retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getCaseNoByString(value);
            }
        }));
        return mapping;
    }




}
