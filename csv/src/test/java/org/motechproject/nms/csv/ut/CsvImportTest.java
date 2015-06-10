package org.motechproject.nms.csv.ut;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvInstanceImporter;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetBoolean;
import org.motechproject.nms.csv.utils.GetInstanceByLong;
import org.motechproject.nms.csv.utils.GetInteger;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.util.CsvContext;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(sampleDataService.findById(eq(1L))).thenReturn(sampleFromDataService);
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowExceptionWhenClosed() throws Exception {
        CsvInstanceImporter<Sample> csvInstanceImporter = createCsvInstanceImporter();
        csvInstanceImporter.read();
    }

    @Test
    public void testReadSampleAsMapWhenValid() throws Exception {
        Reader reader = new StringReader("n,s,o\n12,hello,1");
        CsvMapImporter csvMapImporter = createMapImporter(reader);
        Map<String, Object> record = csvMapImporter.read();
        assertEquals(12L, record.get("number"));
        assertEquals("hello", record.get("string"));
        assertEquals(sampleFromDataService, record.get("object"));
        assertNull(csvMapImporter.read());
    }

    @Test
    public void testReadSampleAsInstanceWhenValid() throws Exception {
        Reader reader = new StringReader("n,s,o\n12,hello,1");
        CsvInstanceImporter<Sample> csvInstanceImporter = createInstanceImporter(reader);
        Sample sample = csvInstanceImporter.read();
        assertEquals(12L, sample.getNumber());
        assertEquals("hello", sample.getString());
        assertEquals(sampleFromDataService, sample.getObject());
        assertNull(csvInstanceImporter.read());
    }

    @Test
    public void testReadSampleWithDifferentColumnsOrderWhenValid() throws Exception {
        Reader reader = new StringReader("n,o,s\n12,1,hello");
        CsvInstanceImporter<Sample> csvInstanceImporter = createInstanceImporter(reader);
        Sample sample = csvInstanceImporter.read();
        assertEquals(12, sample.getNumber());
        assertEquals("hello", sample.getString());
        assertEquals(sampleFromDataService, sample.getObject());
        assertNull(csvInstanceImporter.read());
    }

    @Test(expected = CsvImportDataException.class)
    public void testReadSampleWhenProcessorConstraintIsViolated() throws Exception {
        Reader reader = new StringReader("n,s,o\n12,,1");
        CsvInstanceImporter<Sample> csvInstanceImporter = createInstanceImporter(reader);
        csvInstanceImporter.read();
    }

    @Test
    public void testGetIntegerWhenInputIsValid() throws Exception {
        GetInteger getInteger = createGetInteger();
        assertEquals(12, getInteger.execute("12", csvContext));
        assertEquals(12, getInteger.execute(12, csvContext));
    }

    @Test(expected = CsvImportDataException.class)
    public void testGetIntegerWhenInputIsNull() throws Exception {
        GetInteger getInteger = createGetInteger();
        getInteger.execute(null, csvContext);
    }

    @Test
    public void testGetLongWhenInputIsValid() throws Exception {
        GetLong getLong = createGetLong();
        assertEquals(12L, getLong.execute("12", csvContext));
        assertEquals(12L, getLong.execute(12, csvContext));
    }

    @Test(expected = CsvImportDataException.class)
    public void testGetLongWhenInputIsNull() throws Exception {
        GetLong getLong = createGetLong();

        getLong.execute(null, csvContext);
    }

    @Test
    public void testGetStringWhenInputIsValid() throws Exception {
        GetString getString = createGetString();
        assertEquals("hello", getString.execute("hello", csvContext));
        assertEquals("12", getString.execute(12, csvContext));
        assertEquals("", getString.execute("", csvContext));
    }

    @Test(expected = CsvImportDataException.class)
    public void testGetStringWhenInputIsNull() throws Exception {
        GetString getString = createGetString();

        getString.execute(null, csvContext);
    }

    @Test
    public void testGetBooleanWhenInputIsValid() throws Exception {
        GetBoolean getBoolean = createGetBoolean();
        assertTrue((Boolean) getBoolean.execute("True", csvContext));
        assertTrue((Boolean) getBoolean.execute("t", csvContext));
        assertTrue((Boolean) getBoolean.execute("yes", csvContext));
        assertTrue((Boolean) getBoolean.execute("Y", csvContext));

        assertFalse((Boolean) getBoolean.execute("false", csvContext));
        assertFalse((Boolean) getBoolean.execute("F", csvContext));
        assertFalse((Boolean) getBoolean.execute("NO", csvContext));
        assertFalse((Boolean) getBoolean.execute("n", csvContext));
    }

    @Test(expected = CsvImportDataException.class)
    public void testGetBooleanWhenInputIsNull() throws Exception {
        GetBoolean getBoolean = createGetBoolean();

        getBoolean.execute(null, csvContext);
    }

    @Test(expected = CsvImportDataException.class)
    public void testGetBooleanWhenInputIsInvalid() throws Exception {
        GetBoolean getBoolean = createGetBoolean();

        getBoolean.execute("I'm not a boolean!", csvContext);
    }

    @Test
    public void testGetSampleById() throws Exception {
        GetInstanceByLong<Sample> getSampleById = createGetSampleById();
        assertEquals(sampleFromDataService, getSampleById.execute(1, csvContext));
        assertEquals(sampleFromDataService, getSampleById.execute("1", csvContext));
        assertNull(getSampleById.execute(2, csvContext));
    }

    private CsvMapImporter createMapImporter(Reader reader) throws java.io.IOException {
        return new CsvImporterBuilder()
                .setProcessorMapping(getProcessorMapping())
                .setFieldNameMapping(getFieldNameMapping())
                .createAndOpen(reader);
    }

    private CsvInstanceImporter<Sample> createInstanceImporter(Reader reader) throws java.io.IOException {
        return new CsvImporterBuilder()
                .setProcessorMapping(getProcessorMapping())
                .setFieldNameMapping(getFieldNameMapping())
                .createAndOpen(reader, Sample.class);
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
        mapping.put("n", createGetLong());
        mapping.put("s", createGetString());
        mapping.put("o", createGetSampleById());
        return mapping;
    }

    private CsvInstanceImporter<Sample> createCsvInstanceImporter() {
        return new CsvInstanceImporter<>(Sample.class);
    }

    private CsvMapImporter createCsvMapImporter() {
        return new CsvMapImporter();
    }

    private GetInteger createGetInteger() {
        return new GetInteger();
    }

    private GetLong createGetLong() {
        return new GetLong();
    }

    private GetString createGetString() {
        return new GetString();
    }

    private GetBoolean createGetBoolean() {
        return new GetBoolean();
    }

    private GetInstanceByLong<Sample> createGetSampleById() {
        return new GetInstanceByLong<Sample>() {
            @Override
            public Sample retrieve(Long value) {
                return sampleDataService.findById(value);
            }
        };
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
