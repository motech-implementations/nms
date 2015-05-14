package org.motechproject.nms.imi.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.imi.domain.FileAuditRecord;

import java.util.List;

public interface FileAuditRecordDataService extends MotechDataService<FileAuditRecord> {
    @Lookup
    List<FileAuditRecord> findByFileName(@LookupField(name = "fileName") String fileName);
}
