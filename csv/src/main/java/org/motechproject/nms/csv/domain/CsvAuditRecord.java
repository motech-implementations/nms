package org.motechproject.nms.csv.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Column;
import javax.validation.constraints.NotNull;

@Entity(tableName = "nms_csv_audit_records")
public class CsvAuditRecord {

    public static final int MAX_OUTCOME_LENGTH = 1000; // Includes the "Failure: " string


    // Name of the CSV file, ie: districts.csv
    @Field //todo: what max size?
    @NotNull
    private String file;

    // Which API endpoint was called? ie: /flw/update/language
    @Field
    @NotNull
    private String endpoint;

    // How did the operation work? ie: success
    @Field
    @NotNull
    @Column(length = MAX_OUTCOME_LENGTH)
    private String outcome;

    // Other fields like owner & modification date are already provided by MDS.

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public CsvAuditRecord(String file, String endpoint, String outcome) {
        this.file = file;
        this.endpoint = endpoint;
        this.outcome = outcome;
    }
}
