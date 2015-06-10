package org.motechproject.nms.flw.service.impl;

import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.flw.service.FrontLineWorkerUpdateLanguageImportService;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.prefs.CsvPreference;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service("frontLineWorkerUpdateLanguageImportService")
public class FrontLineWorkerUpdateLanguageImportServiceImpl implements FrontLineWorkerUpdateLanguageImportService {

    public static final String NMS_FLW_ID = "NMS FLW-ID";
    public static final String MCTS_FLW_ID = "MCTS FLW-ID";
    public static final String MSISDN = "MSISDN";
    public static final String LANGUAGE_CODE = "LANGUAGE CODE";

    private FrontLineWorkerService frontLineWorkerService;
    private LanguageService languageService;

    /*
        Expected file format:
        * First line contains headers: NMS FLW-ID, MCTS FLW-ID, MSISDN, LANGUAGE CODE
        * CSV data (comma-separated)
     */
    @Override
    @Transactional
    public void importData(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);

        CsvMapImporter csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(getProcessorMapping())
                .setPreferences(CsvPreference.STANDARD_PREFERENCE)
                .createAndOpen(bufferedReader);
        try {
            Map<String, Object> record;
            while (null != (record = csvImporter.read())) {
                FrontLineWorker flw = flwFromRecord(record);
                if (flw == null) {
                    throw new CsvImportDataException(createErrorMessage(String.format("Unable to locate FLW: %s(%s) %s(%s) %s(%s)",
                                                                                      NMS_FLW_ID, record.get(NMS_FLW_ID),
                                                                                      MCTS_FLW_ID, record.get(MCTS_FLW_ID),
                                                                                      MSISDN, record.get(MSISDN)),
                                                                        csvImporter.getRowNumber()));
                }

                Language language = languageService.getForCode( (String) record.get(LANGUAGE_CODE));
                if (language == null) {
                    throw new CsvImportDataException(createErrorMessage(String.format("Unable to locate language: %s(%s)",
                                                                                      LANGUAGE_CODE, record.get(LANGUAGE_CODE)),
                                                                            csvImporter.getRowNumber()));
                }
                flw.setLanguage(language);
                frontLineWorkerService.update(flw);
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(createErrorMessage(e.getConstraintViolations(), csvImporter.getRowNumber()), e);
        }
    }

    private FrontLineWorker flwFromRecord(Map<String, Object> record) {
        FrontLineWorker flw = null;

        String nmsFlWId = (String) record.get(NMS_FLW_ID);
        String mctsFlwId = (String) record.get(MCTS_FLW_ID);
        Long msisdn = (Long) record.get(MSISDN);

        flw = frontLineWorkerService.getByFlwId(nmsFlWId);

        if ( flw == null) {
            flw = frontLineWorkerService.getByMctsFlwId(mctsFlwId);
        }

        if (flw == null) {
            flw = frontLineWorkerService.getByContactNumber(msisdn);
        }

        return flw;
    }

    private Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(NMS_FLW_ID, new Optional(new GetString()));
        mapping.put(MCTS_FLW_ID, new Optional(new GetString()));
        mapping.put(MSISDN, new Optional(new GetLong()));
        mapping.put(LANGUAGE_CODE, new GetString());

        return mapping;
    }

    private String createErrorMessage(String message, int rowNumber) {
        return String.format("CSV instance error [row: %d]: %s", rowNumber, message);
    }

    private String createErrorMessage(Set<ConstraintViolation<?>> violations, int rowNumber) {
        return String.format("CSV instance error [row: %d]: validation failed for instance of type %s, violations: %s",
                rowNumber, FrontLineWorker.class.getName(), ConstraintViolationUtils.toString(violations));
    }

    @Autowired
    public void setFrontLineWorkerService(FrontLineWorkerService frontLineWorkerService) {
        this.frontLineWorkerService = frontLineWorkerService;
    }

    @Autowired
    public void setLanguageService(LanguageService languageService) {
        this.languageService = languageService;
    }
}
