package org.motechproject.nms.flwUpdate.service.impl;

import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetInstanceByLong;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.flwUpdate.service.FrontLineWorkerUpdateImportService;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.StateDataService;
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

@Service("frontLineWorkerUpdateImportService")
public class FrontLineWorkerUpdateImportServiceImpl implements FrontLineWorkerUpdateImportService {

    public static final String NMS_FLW_ID = "NMS FLW-ID";
    public static final String MCTS_FLW_ID = "MCTS FLW-ID";
    public static final String STATE = "STATE";
    public static final String MSISDN = "MSISDN";
    public static final String LANGUAGE_CODE = "LANGUAGE CODE";
    public static final String NEW_MSISDN = "NEW MSISDN";

    private FrontLineWorkerService frontLineWorkerService;
    private LanguageService languageService;
    private StateDataService stateDataService;
    private MobileAcademyService mobileAcademyService;

    /*
        Expected file format:
        * First line contains headers: NMS FLW-ID, MCTS FLW-ID, MSISDN, LANGUAGE CODE
        * CSV data (comma-separated)
     */
    @Override
    @Transactional
    public void importLanguageData(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);

        CsvMapImporter csvImporter = new CsvImporterBuilder().setProcessorMapping(getLanguageProcessorMapping())
                                                             .setPreferences(CsvPreference.STANDARD_PREFERENCE)
                                                             .createAndOpen(bufferedReader);
        try {
            Map<String, Object> record;
            while (null != (record = csvImporter.read())) {
                Language language = languageService.getForCode((String) record.get(LANGUAGE_CODE));
                if (language == null) {
                    throw new CsvImportDataException(createErrorMessage(String.format("Unable to locate language: %s(%s)",
                                    LANGUAGE_CODE, record.get(LANGUAGE_CODE)),
                            csvImporter.getRowNumber()));
                }

                FrontLineWorker flw = flwFromRecord(record);
                if (flw == null) {
                    throw new CsvImportDataException(createErrorMessage(String.format("Unable to locate FLW: %s(%s) %s(%s) %s(%s)",
                                    NMS_FLW_ID, record.get(NMS_FLW_ID),
                                    MCTS_FLW_ID, record.get(MCTS_FLW_ID),
                                    MSISDN, record.get(MSISDN)),
                                                                        csvImporter.getRowNumber()));
                }

                flw.setLanguage(language);
                frontLineWorkerService.update(flw);
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(createErrorMessage(e.getConstraintViolations(), csvImporter.getRowNumber()), e);
        }
    }

    /*
        Expected file format:
        * First line contains headers: NMS FLW-ID, MCTS FLW-ID, MSISDN, NEW MSISDN
        * CSV data (comma-separated)
     */
    @Override
    @Transactional
    public void importMSISDNData(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);

        CsvMapImporter csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(getMSISDNProcessorMapping())
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

                Long msisdn = (Long) record.get(NEW_MSISDN);

                FrontLineWorker flwWithNewMSISDN = frontLineWorkerService.getByContactNumber(msisdn);

                if (flwWithNewMSISDN != null && flwWithNewMSISDN != flw) {
                    throw new CsvImportDataException(
                            createErrorMessage(String
                                            .format("Attempt to assign an msisdn when an existing FLW " +
                                                            "already has that number FLW in CSV: %s(%s) %s(%s) %s(%s) " +
                                                            "Existing FLW:  %s(%s) %s(%s) %s(%s)",
                                                    NMS_FLW_ID, record.get(NMS_FLW_ID),
                                                    MCTS_FLW_ID, record.get(MCTS_FLW_ID),
                                                    MSISDN, record.get(MSISDN),
                                                    NMS_FLW_ID, flwWithNewMSISDN.getFlwId(),
                                                    MCTS_FLW_ID, flwWithNewMSISDN.getMctsFlwId(),
                                                    MSISDN, flwWithNewMSISDN.getContactNumber()),
                                    csvImporter.getRowNumber()));
                }

                Long oldMsisdn = flw.getContactNumber();
                flw.setContactNumber(msisdn);
                frontLineWorkerService.update(flw);
                mobileAcademyService.updateMsisdn(flw.getId(), oldMsisdn, msisdn);
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(createErrorMessage(e.getConstraintViolations(), csvImporter.getRowNumber()), e);
        } catch (NumberFormatException e) {
            throw new CsvImportDataException(createErrorMessage("Invalid number", csvImporter.getRowNumber()), e);
        }
    }

    private FrontLineWorker flwFromRecord(Map<String, Object> record) {
        FrontLineWorker flw = null;

        String nmsFlWId = (String) record.get(NMS_FLW_ID);
        String mctsFlwId = (String) record.get(MCTS_FLW_ID);
        State state = (State) record.get(STATE);
        Long msisdn = (Long) record.get(MSISDN);

        if (nmsFlWId != null) {
            flw = frontLineWorkerService.getByFlwId(nmsFlWId);
        }

        if (flw == null && mctsFlwId != null) {
            flw = frontLineWorkerService.getByMctsFlwIdAndState(mctsFlwId, state);
        }

        if (flw == null && msisdn != null) {
            flw = frontLineWorkerService.getByContactNumber(msisdn);
        }

        return flw;
    }


    private Map<String, CellProcessor> getIDProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(NMS_FLW_ID, new Optional(new GetString()));
        mapping.put(MCTS_FLW_ID, new Optional(new GetString()));
        mapping.put(MSISDN, new Optional(new GetLong()));
        mapping.put(STATE, new GetInstanceByLong<State>() {
            @Override
            public State retrieve(Long value) {
                return stateDataService.findByCode(value);
            }
        });

        return mapping;
    }

    private Map<String, CellProcessor> getLanguageProcessorMapping() {
        Map<String, CellProcessor> mapping = getIDProcessorMapping();

        mapping.put(LANGUAGE_CODE, new GetString());

        return mapping;
    }

    private Map<String, CellProcessor> getMSISDNProcessorMapping() {
        Map<String, CellProcessor> mapping = getIDProcessorMapping();

        mapping.put(NEW_MSISDN, new GetInstanceByString<Long>() {
            @Override
            public Long retrieve(String value) {
                if (value.length() < 10) {
                    throw new NumberFormatException(String.format("%s too short, must be at least 10 digits", NEW_MSISDN));
                }
                String msisdn = value.substring(value.length() - 10);

                return Long.parseLong(msisdn);
            }
        });

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

    @Autowired
    public void setStateDataService(StateDataService stateDataService) {
        this.stateDataService = stateDataService;
    }

    @Autowired
    public void setMobileAcademyService(MobileAcademyService mobileAcademyService) {
        this.mobileAcademyService = mobileAcademyService;
    }

}
