package org.motechproject.nms.testing.it.csv;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.csv.domain.CsvAuditRecord;
import org.motechproject.nms.csv.repository.CsvAuditRecordDataService;
import org.motechproject.nms.csv.service.CsvAuditService;
import org.motechproject.nms.csv.service.impl.CsvAuditServiceImpl;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class CsvAuditServiceBundleIT extends BasePaxIT {

    @Inject
    TestingService testingService;

    @Inject
    CsvAuditService csvAuditService;

    @Inject
    CsvAuditRecordDataService csvAuditRecordDataService;

    @Before
    public void clearDatabase() {
        testingService.clearDatabase();
    }

    @Test
    public void verifyAuditServiceFunctional() {
        csvAuditService.auditSuccess("foo", "bar");

        List<CsvAuditRecord> records = csvAuditRecordDataService.retrieveAll();

        assertEquals(1, records.size());
        assertEquals("foo", records.get(0).getFile());
        assertEquals("bar", records.get(0).getEndpoint());
        assertEquals(CsvAuditServiceImpl.SUCCESS, records.get(0).getOutcome());
    }

    @Test
    public void verifyTruncatedOutcome() {

        // Build a character string with 10 more characters than the maximum allowed in
        // CsvAuditRecord.MAX_OUTCOME_LENGTH
        StringBuilder sb = new StringBuilder();
        for (int i = 0 ; i < CsvAuditRecord.MAX_OUTCOME_LENGTH + 10 ; i++) {
            sb.append("x");
        }
        csvAuditService.auditFailure("foo", "bar", sb.toString());

        List<CsvAuditRecord> records = csvAuditRecordDataService.retrieveAll();

        assertEquals(1, records.size());
        assertEquals("foo", records.get(0).getFile());
        assertEquals("bar", records.get(0).getEndpoint());
        assertEquals(CsvAuditRecord.MAX_OUTCOME_LENGTH, records.get(0).getOutcome().length());
    }
}
