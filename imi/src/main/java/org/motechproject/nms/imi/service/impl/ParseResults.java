package org.motechproject.nms.imi.service.impl;

import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;

import java.util.List;
import java.util.Map;

public class ParseResults {
    private Map<String, CallSummaryRecordDto> records;
    private List<String> errors;

    public ParseResults(Map<String, CallSummaryRecordDto> records, List<String> errors) {
        this.records = records;
        this.errors = errors;
    }

    public Map<String, CallSummaryRecordDto> getRecords() {
        return records;
    }

    public List<String> getErrors() {
        return errors;
    }
}
