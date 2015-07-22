package org.motechproject.nms.api.web;

import org.motechproject.alerts.contract.AlertCriteria;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.Alert;
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
        List<Alert> alerts = alertService.search(new AlertCriteria());
        if (alerts.size() == 0) {
            return "OK";
        }
        return String.format("%d ALERT%s", alerts.size(), alerts.size() == 1 ? "" : "S");
    }


}
