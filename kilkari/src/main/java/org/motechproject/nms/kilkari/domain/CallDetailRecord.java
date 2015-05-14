package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.Ignore;
import org.motechproject.nms.props.domain.CallStatus;

import javax.jdo.annotations.Column;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

@Entity(tableName = "nms_kilkari_cdrs")
public class CallDetailRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Field
    private String requestId;

    @Field
    private String serviceId;

    @Field
    @Min(value = 1000000000L, message = "msisdn must be 10 digits")
    @Max(value = 9999999999L, message = "msisdn must be 10 digits")
    @Column(length = 10)
    private Long msisdn;

    @Field
    private String cli;

    @Field
    private Integer priority;

    @Field
    private String callFlowUrl;

    @Field
    private String contentFileName;

    @Field
    private String weekId;

    @Field
    private String languageLocationCode;

    @Field
    private String circle;

    @Field
    private CallStatus finalStatus;

    @Field
    private Integer statusCode;

    @Field
    private Integer attempts;

    static final int NUMBER_OF_FIELDS = 13;

    public CallDetailRecord() { }

    public CallDetailRecord(String requestId, String serviceId, // NO CHECKSTYLE More than 7 parameters
                            Long msisdn, String cli, Integer priority, String callFlowUrl, String contentFileName,
                            String weekId, String languageLocationCode, String circle, CallStatus finalStatus,
                            Integer statusCode, Integer attempts) {
        this.requestId = requestId;
        this.serviceId = serviceId;
        this.msisdn = msisdn;
        this.cli = cli;
        this.priority = priority;
        this.callFlowUrl = callFlowUrl;
        this.contentFileName = contentFileName;
        this.weekId = weekId;
        this.languageLocationCode = languageLocationCode;
        this.circle = circle;
        this.finalStatus = finalStatus;
        this.statusCode = statusCode;
        this.attempts = attempts;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Long getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(Long msisdn) {
        this.msisdn = msisdn;
    }

    public String getCli() {
        return cli;
    }

    public void setCli(String cli) {
        this.cli = cli;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getCallFlowUrl() {
        return callFlowUrl;
    }

    public void setCallFlowUrl(String callFlowUrl) {
        this.callFlowUrl = callFlowUrl;
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

    public String getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(String languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    public CallStatus getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(CallStatus finalStatus) {
        this.finalStatus = finalStatus;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }


    @Ignore
    public static CallDetailRecord fromCsvLine(String line) {
        String[] fields = line.split(",");
        if (fields.length != NUMBER_OF_FIELDS) {
            throw new IllegalStateException(String.format("Wrong number of fields, expecting %d but seeing %d",
                    NUMBER_OF_FIELDS,
                    fields.length));
        }

        int i = 0;
        return new CallDetailRecord(
                fields[i++], //requestId
                fields[i++], //serviceId
                Long.parseLong(fields[i++]), //msisdn
                fields[i++], //cli
                Integer.parseInt(fields[i++]), //priority - NOTE: may throw a NumberFormatException
                fields[i++], //callFlowUrl
                fields[i++], //contentFileName
                fields[i++], //weekId
                fields[i++], //languageLocationCode
                fields[i++], //circle
                // NOTE: may throw a NumberFormatException or IllegalArgumentException
                CallStatus.fromInt(Integer.parseInt(fields[i++])), //finalStatus
                Integer.parseInt(fields[i++]), //statusCode - NOTE: may throw a NumberFormatException
                Integer.parseInt(fields[i++]) //attempts - NOTE: may throw a NumberFormatException
        );
    }


    @Ignore
    public String toCsvLine() {
        StringBuilder sb = new StringBuilder(requestId);
        sb.append(",");
        sb.append(serviceId);
        sb.append(",");
        sb.append(msisdn);
        sb.append(",");
        sb.append(cli);
        sb.append(",");
        sb.append(priority);
        sb.append(",");
        sb.append(callFlowUrl);
        sb.append(",");
        sb.append(contentFileName);
        sb.append(",");
        sb.append(weekId);
        sb.append(",");
        sb.append(languageLocationCode);
        sb.append(",");
        sb.append(circle);
        sb.append(",");
        sb.append(finalStatus.getValue());
        sb.append(",");
        sb.append(statusCode);
        sb.append(",");
        sb.append(attempts);
        return sb.toString();
    }

    // todo: verify: We assume weekId is just a string version of week, is that true?
    @Ignore
    public int getWeek() {
        return Integer.valueOf(weekId);
    }


    @Override //NO CHECKSTYLE Cyclomatic Complexity
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CallDetailRecord that = (CallDetailRecord) o;

        if (!requestId.equals(that.requestId)) {
            return false;
        }
        if (!serviceId.equals(that.serviceId)) {
            return false;
        }
        if (!msisdn.equals(that.msisdn)) {
            return false;
        }
        if (cli != null ? !cli.equals(that.cli) : that.cli != null) {
            return false;
        }
        if (!priority.equals(that.priority)) {
            return false;
        }
        if (callFlowUrl != null ? !callFlowUrl.equals(that.callFlowUrl) : that.callFlowUrl != null) {
            return false;
        }
        if (!contentFileName.equals(that.contentFileName)) {
            return false;
        }
        if (!weekId.equals(that.weekId)) {
            return false;
        }
        if (!languageLocationCode.equals(that.languageLocationCode)) {
            return false;
        }
        if (!circle.equals(that.circle)) {
            return false;
        }
        if (finalStatus != that.finalStatus) {
            return false;
        }
        if (!statusCode.equals(that.statusCode)) {
            return false;
        }
        return attempts.equals(that.attempts);

    }

    @Override //NO CHECKSTYLE Cyclomatic Complexity
    public int hashCode() {
        int result = (requestId != null ? requestId.hashCode() : 0);
        result = 31 * result + (serviceId != null ? serviceId.hashCode() : 0);
        result = 31 * result + (msisdn != null ? msisdn.hashCode() : 0);
        result = 31 * result + (cli != null ? cli.hashCode() : 0);
        result = 31 * result + (priority != null ? priority.hashCode() : 0);
        result = 31 * result + (callFlowUrl != null ? callFlowUrl.hashCode() : 0);
        result = 31 * result + (contentFileName != null ? contentFileName.hashCode() : 0);
        result = 31 * result + (weekId != null ? weekId.hashCode() : 0);
        result = 31 * result + (languageLocationCode != null ? languageLocationCode.hashCode() : 0);
        result = 31 * result + (circle != null ? circle.hashCode() : 0);
        result = 31 * result + (finalStatus != null ? finalStatus.hashCode() : 0);
        result = 31 * result + (statusCode != null ? statusCode.hashCode() : 0);
        result = 31 * result + (attempts != null ? attempts.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CallDetailRecord{" +
                "requestId='" + requestId + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", msisdn='" + msisdn + '\'' +
                " ...}";
    }
}
