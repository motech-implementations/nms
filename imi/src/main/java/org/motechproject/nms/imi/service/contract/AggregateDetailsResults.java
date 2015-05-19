package org.motechproject.nms.imi.service.contract;

import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;

import java.util.List;
import java.util.Map;

public class AggregateDetailsResults {
    private Map<String, CallSummaryRecordDto> records;
    private List<String> errors;

    public AggregateDetailsResults(Map<String, CallSummaryRecordDto> records, List<String> errors) {
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
