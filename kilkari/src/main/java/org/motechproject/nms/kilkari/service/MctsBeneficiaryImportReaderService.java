package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

public interface MctsBeneficiaryImportReaderService {

    int importChildData(Reader reader, SubscriptionOrigin origin) throws IOException;

    int importMotherData(Reader reader, SubscriptionOrigin importOrigin) throws IOException;

    List<Map<String, Object>> readCsv(BufferedReader bufferedReader, Map<String, CellProcessor> cellProcessorMapper) throws IOException;

    Map<String, CellProcessor> getRchChildProcessorMapping();
}
