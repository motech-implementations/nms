package org.motechproject.nms.imi.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;

/**
 * CallSummaryRecord Processing Audit - records CSR chunk processing information
 */
@Entity(tableName = "nms_imi_chunk_audit_records")
@Unique(name = "UNIQUE_CHUNK_AUDIT_RECORD_COMPOSITE_IDX", members = { "file", "chunk" })
public class ChunkAuditRecord {

    @Field
    @Column(allowsNull = "false")
    private String file;

    @Field
    @Column(allowsNull = "false")
    private String chunk;

    @Field
    @Column(allowsNull = "false")
    private int csrToProcess;

    @Field
    private int csrProcessed;

    @Field
    private String node;

    @Field
    private DateTime processingStart;

    @Field
    private DateTime processingEnd;

    @Field
    private String timing;

    public ChunkAuditRecord() {
    }

    public ChunkAuditRecord(String file, String chunk, int csrToProcess) {
        this.file = file;
        this.chunk = chunk;
        this.csrToProcess = csrToProcess;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getChunk() {
        return chunk;
    }

    public void setChunk(String chunk) {
        this.chunk = chunk;
    }

    public int getCsrToProcess() {
        return csrToProcess;
    }

    public void setCsrToProcess(int csrToProcess) {
        this.csrToProcess = csrToProcess;
    }

    public int getCsrProcessed() {
        return csrProcessed;
    }

    public void setCsrProcessed(int csrProcessed) {
        this.csrProcessed = csrProcessed;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getTiming() {
        return timing;
    }

    public void setTiming(String timing) {
        this.timing = timing;
    }

    public DateTime getProcessingStart() {
        return processingStart;
    }

    public void setProcessingStart(DateTime processingStart) {
        this.processingStart = processingStart;
    }

    public DateTime getProcessingEnd() {
        return processingEnd;
    }

    public void setProcessingEnd(DateTime processingEnd) {
        this.processingEnd = processingEnd;
    }
}
