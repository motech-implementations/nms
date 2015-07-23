package org.motechproject.nms.api.web;

import org.motechproject.alerts.contract.AlertCriteria;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.Alert;
import org.motechproject.alerts.domain.AlertStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class StatusController {
    @Autowired
    private AlertService alertService;

    @RequestMapping("/status")
    @ResponseBody
    public String status() {
        List<Alert> newAlerts = alertService.search(new AlertCriteria().byStatus(AlertStatus.NEW));
        List<Alert> readAlerts = alertService.search(new AlertCriteria().byStatus(AlertStatus.READ));
        if (newAlerts.size() == 0 && readAlerts.size() == 0) {
            return "OK";
        }
        String ret = "";
        if (newAlerts.size() > 0) {
            ret += String.format("%d NEW ALERT%s", newAlerts.size(), newAlerts.size() == 1 ? "" : "S");
        }
        if (readAlerts.size() > 0) {
            ret += ret.length() == 0 ? "" : " AND ";
            ret += String.format("%d READ ALERT%s", readAlerts.size(), readAlerts.size() == 1 ? "" : "S");
        }
        return ret;
    }


}
