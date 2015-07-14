package org.motechproject.nms.tracking.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

@Entity
public class ChangeLog {

    @Field(required = true)
    private String entityName;

    @Field(required = true)
    private Long instanceId;

    @Field(required = true)
    private DateTime timestamp;

    @Field(required = true)
    private String change;

    public ChangeLog() {
    }

    public ChangeLog(String entityName, Long instanceId, DateTime timestamp, String change) {
        this.entityName = entityName;
        this.instanceId = instanceId;
        this.timestamp = timestamp;
        this.change = change;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }
}
