package org.motechproject.nms.api.web.contract;

public class SubscriptionRequest {
    private String callingNumber;
    private String operator;
    private String circle;
    private String callId;
    private Integer languageLocationCode;
    private String subscriptionPack;

    public SubscriptionRequest() {
    }

    public SubscriptionRequest(String callingNumber, String operator, String circle, String callId, 
        Integer languageLocationCode, String subscriptionPack) {
        this.callingNumber = callingNumber;
        this.operator = operator;
        this.circle = circle;
        this.callId = callId;
        this.languageLocationCode = languageLocationCode;
        this.subscriptionPack = subscriptionPack;
    }

    public String getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(String callingNumber) {
        this.callingNumber = callingNumber;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public Integer getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(Integer languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }

    public String getSubscriptionPack() {
        return subscriptionPack;
    }

    public void setSubscriptionPack(String subscriptionPack) {
        this.subscriptionPack = subscriptionPack;
    }

}
