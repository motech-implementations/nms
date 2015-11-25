package org.motechproject.nms.imi.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.imi.domain.ChunkAuditRecord;

import java.util.List;

public interface CsrProcessingAuditDataService extends MotechDataService<ChunkAuditRecord> {
    @Lookup
    ChunkAuditRecord findByFileAndChunk(@LookupField(name = "file") String file,
                                        @LookupField(name = "chunk") String chunk);
    @Lookup
    List<ChunkAuditRecord> findByFile(@LookupField(name = "file") String file);

    long countFindByFile(@LookupField(name = "file") String file);
}
