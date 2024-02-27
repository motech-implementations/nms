package org.motechproject.nms.imi.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.kilkari.domain.WhatsAppMessageStatus;
import org.motechproject.nms.kilkari.dto.WhatsAppOptCsrDto;
import org.motechproject.nms.kilkari.exception.InvalidCallRecordDataException;
import org.motechproject.nms.props.domain.RequestId;

@Entity(tableName = "nms_imi_wp_csr")
public class WhatsAppOptCsr {
    @Field
    private String externalId;

    @Field
    private Long urn;

    @Field
    private String contentFileName;

    @Field
    private String weekId;

    @Field
    private String messageStatusTimestamp;

    @Field
    private WhatsAppMessageStatus messageStatus;

    @Field
    private String preferedLanguage;

    @Field
    private long stateCode;


    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Long getUrn() {
        return urn;
    }

    public void setUrn(Long urn) {
        this.urn = urn;
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

    public String getMessageStatusTimestamp() {
        return messageStatusTimestamp;
    }

    public void setMessageStatusTimestamp(String messageStatusTimestamp) {
        this.messageStatusTimestamp = messageStatusTimestamp;
    }

    public WhatsAppMessageStatus getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(WhatsAppMessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public WhatsAppOptCsr() {
    }

    public String getPreferedLanguage() {
        return preferedLanguage;
    }

    public void setPreferedLanguage(String preferedLanguage) {
        this.preferedLanguage = preferedLanguage;
    }

    public long getStateCode() {
        return stateCode;
    }

    public void setStateCode(long stateCode) {
        this.stateCode = stateCode;
    }

    public WhatsAppOptCsr(String externalId, Long urn, String contentFileName, String weekId, String messageStatusTimestamp, WhatsAppMessageStatus messageStatus) {
        this.externalId = externalId;
        this.urn = urn;
        this.contentFileName = contentFileName;
        this.weekId = weekId;
        this.messageStatusTimestamp = messageStatusTimestamp;
        this.messageStatus = messageStatus;
    }



    public WhatsAppOptCsrDto toDto() {
        String subscriptionId;
        try {
            RequestId r = RequestId.fromString(externalId);
            subscriptionId = r.getSubscriptionId();
        } catch (IllegalArgumentException e) {
            throw new InvalidCallRecordDataException(e);
        }
        return new WhatsAppOptCsrDto(
                subscriptionId,
                urn,
                contentFileName,
                weekId,
                preferedLanguage,
                stateCode,
                messageStatusTimestamp,
                messageStatus
        );
    }
}
