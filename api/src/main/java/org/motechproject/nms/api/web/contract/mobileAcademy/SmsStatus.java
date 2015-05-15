package org.motechproject.nms.api.web.contract.mobileAcademy;

import org.motechproject.nms.api.web.contract.mobileAcademy.sms.RequestData;

/**
 * Sms status object sent to NMS from IMI
 */
public class SmsStatus {

    private RequestData requestData;

    public SmsStatus() {
    }

    public RequestData getRequestData() {
        return requestData;
    }

    public void setRequestData(RequestData requestData) {
        this.requestData = requestData;
    }
}