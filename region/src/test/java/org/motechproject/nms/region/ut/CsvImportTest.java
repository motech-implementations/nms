package org.motechproject.nms.region.ut;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.region.exception.CsvImportDataException;
import org.motechproject.nms.region.utils.CsvImporter;
import org.motechproject.nms.region.utils.GetInstanceByLong;
import org.motechproject.nms.region.utils.GetInteger;
import org.motechproject.nms.region.utils.GetLong;
import org.motechproject.nms.region.utils.GetString;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.util.CsvContext;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CsvImportTest {

    @Mock
    private MotechDataService<Sample> sampleDataService;

    @Mock
    private Sample sampleFromDataService;

    @Mock
    private CsvContext csvContext;

    private CsvImporter<Sample> csvImporter;

    private GetInteger getInteger;

    private GetLong getLong;

    private GetString getString;

    private GetInstanceByLong<Sample> getSampleById;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        csvImporter = new CsvImporter<>(Sample.class);
        getInteger = new GetInteger();
        getLong = new GetLong();
        getString = new GetString();
        getSampleById = new GetInstanceByLong<Sample>() {
            @Override
            public Sample retrieve(Long value) {
                return sampleDataService.findById(value);
            }
        };
        when(sampleDataService.findById(eq(1L))).thenReturn(sampleFromDataService);
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowExceptionWhenClosed() throws Exception {
        csvImporter.read();
    }

    @Test
    public void testReadSampleWhenValid() throws Exception {
        Reader reader = new StringReader("n,s,o\n12,hello,1");
        csvImporter.open(reader, getFieldNameMapping(), getProcessorMapping());
        Sample sample = csvImporter.read();
        assertEquals(12, sample.getNumber());
        assertEquals("hello", sample.getString());
        assertEquals(sampleFromDataService, sample.getObject());
        assertNull(csvImporter.read());
    }

    @Test
    public void testReadSampleWithDifferentColumnsOrderWhenValid() throws Exception {
        Reader reader = new StringReader("n,o,s\n12,1,hello");
        csvImporter.open(reader, getFieldNameMapping(), getProcessorMapping());
        Sample sample = csvImporter.read();
        assertEquals(12, sample.getNumber());
        assertEquals("hello", sample.getString());
        assertEquals(sampleFromDataService, sample.getObject());
        assertNull(csvImporter.read());
    }

    @Test(expected = CsvImportDataException.class)
    public void testReadSampleWhenProcessorConstraintIsViolated() throws Exception {
        Reader reader = new StringReader("n,s,o\n12,,1");
        csvImporter.open(reader, getFieldNameMapping(), getProcessorMapping());
        csvImporter.read();
    }

    @Test
    public void testGetIntegerWhenInputIsValid() throws Exception {
        assertEquals(12, getInteger.execute("12", csvContext));
        assertEquals(12, getInteger.execute(12, csvContext));
    }

    @Test(expected = CsvImportDataException.class)
    public void testGetIntegerWhenInputIsNull() throws Exception {
        getInteger.execute(null, csvContext);
    }

    @Test
    public void testGetLongWhenInputIsValid() throws Exception {
        assertEquals(12L, getLong.execute("12", csvContext));
        assertEquals(12L, getLong.execute(12, csvContext));
    }

    @Test(expected = CsvImportDataException.class)
    public void testGetLongWhenInputIsNull() throws Exception {
        getLong.execute(null, csvContext);
    }

    @Test
    public void testGetStringWhenInputIsValid() throws Exception {
        assertEquals("hello", getString.execute("hello", csvContext));
        assertEquals("12", getString.execute(12, csvContext));
        assertEquals("", getString.execute("", csvContext));
    }

    @Test(expected = CsvImportDataException.class)
    public void testGetStringWhenInputIsNull() throws Exception {
        getString.execute(null, csvContext);
    }

    @Test
    public void testGetSampleById() throws Exception {
        assertEquals(sampleFromDataService, getSampleById.execute(1, csvContext));
        assertEquals(sampleFromDataService, getSampleById.execute("1", csvContext));
        assertNull(getSampleById.execute(2, csvContext));
    }

    private Map<String, String> getFieldNameMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("n", "number");
        mapping.put("s", "string");
        mapping.put("o", "object");
        return mapping;
    }

    private Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put("n", getLong);
        mapping.put("s", getString);
        mapping.put("o", getSampleById);
        return mapping;
    }

    public static class Sample {

        private long number;
        private String string;
        private Sample object;

        public long getNumber() {
            return number;
        }

        public void setNumber(long number) {
            this.number = number;
        }

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }

        public Sample getObject() {
            return object;
        }

        public void setObject(Sample object) {
            this.object = object;
        }
    }
}
