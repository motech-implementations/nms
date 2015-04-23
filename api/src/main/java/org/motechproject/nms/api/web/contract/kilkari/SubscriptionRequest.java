package org.motechproject.nms.api.web.contract.kilkari;

public class SubscriptionRequest {
    private Long callingNumber;
    private String operator;
    private String circle;
    private Long callId;
    private String languageLocationCode;
    private String subscriptionPack;
    private String subscriptionId;

    public SubscriptionRequest() {
    }

    public SubscriptionRequest(Long callingNumber, String operator, String circle, Long callId,
        String languageLocationCode, String subscriptionPack) {
        this.callingNumber = callingNumber;
        this.operator = operator;
        this.circle = circle;
        this.callId = callId;
        this.languageLocationCode = languageLocationCode;
        this.subscriptionPack = subscriptionPack;
    }

    public SubscriptionRequest(Long calledNumber, String operator, String circle, Long callId,
                               String subscriptionId) {
        this.callingNumber = calledNumber;
        this.operator = operator;
        this.circle = circle;
        this.callId = callId;
        this.subscriptionId = subscriptionId;
    }

    public Long getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(Long callingNumber) {
        this.callingNumber = callingNumber;
    }

    /**
     *  This extra getter/setter pair aliases calledNumber to callingNumber because the spec has different names
     *  for this field depending on the type of request.
     */
    public Long getCalledNumber() { return callingNumber; }

    public void setCalledNumber(Long calledNumber) { this.callingNumber = calledNumber; }

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

    public Long getCallId() {
        return callId;
    }

    public void setCallId(Long callId) {
        this.callId = callId;
    }

    public String getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(String languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }

    public String getSubscriptionPack() {
        return subscriptionPack;
    }

    public void setSubscriptionPack(String subscriptionPack) {
        this.subscriptionPack = subscriptionPack;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

}
