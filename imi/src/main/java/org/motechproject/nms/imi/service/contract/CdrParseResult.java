package org.motechproject.nms.imi.service.contract;


import org.motechproject.nms.kilkari.domain.CallDetailRecord;

import java.util.List;

public class CdrParseResult {
    private List<CallDetailRecord> cdrs;
    private List<String> errors;

    public CdrParseResult(List<CallDetailRecord> cdrs, List<String> errors) {
        this.cdrs = cdrs;
        this.errors = errors;
    }

    public List<CallDetailRecord> getCdrs() {
        return cdrs;
    }

    public void setCdrs(List<CallDetailRecord> cdrs) {
        this.cdrs = cdrs;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
