package org.motechproject.nms.api.web.service.impl;

import org.motechproject.nms.api.web.contract.AddFlwRequest;
import org.motechproject.nms.api.web.contract.AddRchFlwRequest;
import org.motechproject.nms.api.web.service.FlwCsvService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.kilkari.domain.RejectionReasons;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.rejectionhandler.domain.FlwImportRejection;
import org.motechproject.nms.rejectionhandler.service.FlwRejectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by vishnu on 25/9/17.
 */
@Service("flwRejectionCsvService")
public class FlwCsvServiceImpl implements FlwCsvService {

    @Autowired
    private FlwRejectionService flwRejectionService;

    @Autowired
    private FrontLineWorkerService frontLineWorkerService;

    @Autowired
    private StateDataService stateDataService;

    @Override
    public void csvRejectionsMcts(String fieldName, AddFlwRequest addFlwRequest) {
        String action = this.flwActionFinder(addFlwRequest);
        if ("contactNumber".equals(fieldName)) {
            flwRejectionService.createUpdate(flwRejectionMcts(addFlwRequest, false, RejectionReasons.MSISDN_EMPTY_OR_WRONG_FORMAT.toString(), action));
        } else if ("gfStatus".equals(fieldName)) {
            flwRejectionService.createUpdate(flwRejectionMcts(addFlwRequest, false, RejectionReasons.GF_STATUS_EMPTY_OR_WRONG_FORMAT.toString(), action));
        } else if ("type".equals(fieldName)) {
            flwRejectionService.createUpdate(flwRejectionMcts(addFlwRequest, false, RejectionReasons.FLW_TYPE_NOT_ASHA.toString(), action));
        } else {
            flwRejectionService.createUpdate(flwRejectionMcts(addFlwRequest, false, RejectionReasons.FIELD_NOT_PRESENT.toString(), action));
        }
    }

    @Override
    @Transactional
    public void csvRejectionsRch(String fieldName, AddRchFlwRequest addRchFlwRequest) {
        String action = this.rchFlwActionFinder(addRchFlwRequest);
        if ("contactNumber".equals(fieldName)) {
            flwRejectionService.createUpdate(flwRejectionRch(addRchFlwRequest, false, RejectionReasons.MSISDN_EMPTY_OR_WRONG_FORMAT.toString(), action));
        } else if ("gfStatus".equals(fieldName)) {
            flwRejectionService.createUpdate(flwRejectionRch(addRchFlwRequest, false, RejectionReasons.GF_STATUS_EMPTY_OR_WRONG_FORMAT.toString(), action));
        } else if ("type".equals(fieldName)) {
            flwRejectionService.createUpdate(flwRejectionRch(addRchFlwRequest, false, RejectionReasons.FLW_TYPE_NOT_ASHA.toString(), action));
        } else {
            flwRejectionService.createUpdate(flwRejectionRch(addRchFlwRequest, false, RejectionReasons.FIELD_NOT_PRESENT.toString(), action));
        }
    }

    private String flwActionFinder(AddFlwRequest record) {
        if (frontLineWorkerService.getByMctsFlwIdAndState(record.getMctsFlwId(), stateDataService.findByCode(record.getStateId())) == null) {
            return "CREATE";
        } else {
            return "UPDATE";
        }
    }

    private String rchFlwActionFinder(AddRchFlwRequest record) {
        if (frontLineWorkerService.getByMctsFlwIdAndState(record.getFlwId(), stateDataService.findByCode(record.getStateId())) == null) {
            return "CREATE";
        } else {
            return "UPDATE";
        }
    }

    public static FlwImportRejection flwRejectionMcts(AddFlwRequest record, Boolean accepted, String rejectionReason, String action) {
        FlwImportRejection flwImportRejection = new FlwImportRejection();
        flwImportRejection.setGfName(record.getName());
        flwImportRejection.setFlwId(Long.parseLong(record.getMctsFlwId()));
        flwImportRejection.setMsisdn(record.getContactNumber().toString());
        flwImportRejection.setStateId(record.getStateId());
        flwImportRejection.setDistrictId(record.getDistrictId());
        flwImportRejection.setTalukaId(record.getTalukaId());
        flwImportRejection.setPhcId(record.getPhcId());
        flwImportRejection.setSubcentreId(record.getSubcentreId());
        flwImportRejection.setVillageId(record.getVillageId());
        flwImportRejection.setHealthBlockId(record.getHealthblockId());
        flwImportRejection.setType(record.getType());
        flwImportRejection.setGfStatus(record.getGfStatus());
        flwImportRejection.setSource("MCTS-Import");
        flwImportRejection.setAccepted(accepted);
        flwImportRejection.setRejectionReason(rejectionReason);
        flwImportRejection.setAction(action);

        return flwImportRejection;
    }

    public static FlwImportRejection flwRejectionRch(AddRchFlwRequest record, Boolean accepted, String rejectionReason, String action) {
        FlwImportRejection flwImportRejection = new FlwImportRejection();
        flwImportRejection.setGfName(record.getName());
        flwImportRejection.setFlwId(Long.parseLong(record.getFlwId()));
        flwImportRejection.setMsisdn(record.getMsisdn().toString());
        flwImportRejection.setStateId(record.getStateId());
        flwImportRejection.setDistrictId(record.getDistrictId());
        flwImportRejection.setTalukaId(record.getTalukaId());
        flwImportRejection.setPhcId(record.getPhcId());
        flwImportRejection.setSubcentreId(record.getSubcentreId());
        flwImportRejection.setVillageId(record.getVillageId());
        flwImportRejection.setHealthBlockId(record.getHealthblockId());
        flwImportRejection.setType(record.getGfType());
        flwImportRejection.setGfStatus(record.getGfStatus());
        flwImportRejection.setSource("RCH-Import");
        flwImportRejection.setAccepted(accepted);
        flwImportRejection.setRejectionReason(rejectionReason);
        flwImportRejection.setAction(action);

        return flwImportRejection;
    }
}
