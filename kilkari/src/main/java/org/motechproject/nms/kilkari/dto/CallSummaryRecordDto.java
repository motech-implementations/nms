package org.motechproject.nms.kilkari.dto;

import org.motechproject.mds.annotations.Ignore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CallSummaryRecordDto implements Serializable {

    private static final long serialVersionUID = -8391255985224161089L;


    private String subscriptionId;

    private int statusCode;

    private int finalStatus;

    private String contentFileName;

    private String weekId;

    private String languageCode;

    private String circleName;

    public CallSummaryRecordDto() { }

    public CallSummaryRecordDto(String subscriptionId, int statusCode, int finalStatus, String contentFileName,
                                String weekId, String languageCode, String circleName) {
        this.subscriptionId = subscriptionId;
        this.statusCode = statusCode;
        this.finalStatus = finalStatus;
        this.contentFileName = contentFileName;
        this.weekId = weekId;
        this.languageCode = languageCode;
        this.circleName = circleName;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(int finalStatus) {
        this.finalStatus = finalStatus;
    }

    public String getContentFileName() {
        return contentFileName;
    }

    public void setContentFileName(String contentFileName) {
        this.contentFileName = contentFileName;
    }

    public String getWeekId() {
        return weekId;
    }

    public void setWeekId(String weekId) {
        this.weekId = weekId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getCircleName() {
        return circleName;
    }

    public void setCircleName(String circleName) {
        this.circleName = circleName;
    }

    @Ignore
    public static CallSummaryRecordDto fromParams(Map<String, Object> params) {
        CallSummaryRecordDto csr;
        csr = new CallSummaryRecordDto(
                (String) params.get("subscriptionId"),
                (int) params.get("statusCode"),
                (int) params.get("finalStatus"),
                (String) params.get("contentFileName"),
                (String) params.get("weekId"),
                (String) params.get("languageCode"),
                (String) params.get("circleName")
        );
        return csr;
    }

    @Ignore
    public static Map<String, Object> toParams(CallSummaryRecordDto csr) {
        Map<String, Object> params = new HashMap<>();
        params.put("subscriptionId", csr.subscriptionId);
        params.put("statusCode", csr.statusCode);
        params.put("finalStatus", csr.finalStatus);
        params.put("contentFileName", csr.contentFileName);
        params.put("weekId", csr.weekId);
        params.put("languageCode", csr.languageCode);
        params.put("circleName", csr.circleName);
        return params;
    }

    @Override
    public String toString() {
        return "CallSummaryRecordDto{" +
                "subscriptionId='" + subscriptionId + '\'' +
                ", statusCode=" + statusCode +
                ", finalStatus=" + finalStatus +
                ", contentFileName='" + contentFileName + '\'' +
                ", weekId='" + weekId + '\'' +
                ", languageCode='" + languageCode + '\'' +
                ", circleName='" + circleName + '\'' +
                '}';
    }
}
