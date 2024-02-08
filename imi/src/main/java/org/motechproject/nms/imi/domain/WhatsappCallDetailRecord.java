package org.motechproject.nms.imi.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

@Entity(tableName = "nms_imi_whatsapp_cdrs")
public class WhatsappCallDetailRecord {
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
    private String messageStatus;


    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public long getUrn(){
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

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

}
