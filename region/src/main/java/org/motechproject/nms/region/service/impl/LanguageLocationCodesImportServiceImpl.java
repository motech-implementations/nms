package org.motechproject.nms.region.service.impl;

import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.exception.CsvImportDataException;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.LanguageLocationDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.LanguageLocationCodesImportService;
import org.motechproject.nms.region.utils.CsvMapImporter;
import org.motechproject.nms.region.utils.GetBoolean;
import org.motechproject.nms.region.utils.GetInstanceByLong;
import org.motechproject.nms.region.utils.GetInstanceByString;
import org.motechproject.nms.region.utils.GetString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service("languageLocationCodesImportService")
public class LanguageLocationCodesImportServiceImpl implements LanguageLocationCodesImportService {

    public static final Logger LOGGER = LoggerFactory.getLogger(LanguageLocationCodesImportServiceImpl.class);

    public static final String LANGUAGE_LOCATION_CODE = "languageLocationCode";
    public static final String LANGUAGE = "language";
    public static final String CIRCLE = "circle";
    public static final String STATE = "state";
    public static final String DISTRICT = "district";
    public static final String DEFAULT_FOR_CIRCLE = "defaultForCircle";

    private LanguageLocationDataService languageLocationDataService;
    private LanguageDataService languageDataService;
    private CircleDataService circleDataService;
    private StateDataService stateDataService;
    private DistrictDataService districtDataService;

    @Override
    public void importData(Reader reader) throws IOException {
        CsvMapImporter csvImporter = new CsvMapImporter();
        csvImporter.open(reader, getProcessorMapping());
        Map<String, Object> record;
        while (null != (record = csvImporter.read())) {
            importRecord(record);
        }
    }

    private void importRecord(Map<String, Object> record) {
        String languageLocationCode = (String) record.get(LANGUAGE_LOCATION_CODE);
        Language language = (Language) record.get(LANGUAGE);
        State state = (State) record.get(STATE);
        District district = (District) record.get(DISTRICT);
        Circle circle = (Circle) record.get(CIRCLE);
        Boolean defaultForCircle = (Boolean) record.get(DEFAULT_FOR_CIRCLE);

        if (null != state && null != district) {
            importRecordForStateAndDistrict(languageLocationCode, language, circle, defaultForCircle, state, district);
        } else if (null != state) {
            importRecordForState(languageLocationCode, language, circle, defaultForCircle, state);
        } else if (null != district) {
            importRecordForDistrict(languageLocationCode, language, circle, defaultForCircle, district);
        } else {
            throw new CsvImportDataException("Both state and district are null");
        }
    }

    private void importRecordForStateAndDistrict(String languageLocationCode, Language language, Circle circle,
                                                 Boolean defaultForCircle, State state, District district) {
        verify(Objects.equals(district.getState().getCode(), state.getCode()),
                "District's state does not match the state supplied in the CSV record");
        verifyDistrictLanguageLocation(district);
        LanguageLocation languageLocation = getLanguageLocation(languageLocationCode, language, circle, defaultForCircle);
        languageLocation.getDistrictSet().add(district);
    }

    private void importRecordForState(String languageLocationCode, Language language, Circle circle,
                                      Boolean defaultForCircle, State state) {
        for (District stateDistrict : state.getDistricts()) {
            verifyDistrictLanguageLocation(stateDistrict);
        }
        LanguageLocation languageLocation = getLanguageLocation(languageLocationCode, language, circle, defaultForCircle);
        languageLocation.getDistrictSet().addAll(state.getDistricts());
    }

    private void importRecordForDistrict(String languageLocationCode, Language language, Circle circle,
                                         Boolean defaultForCircle, District district) {
        LOGGER.warn("State not specified for the '{}' district", district.getName());
        verifyDistrictLanguageLocation(district);
        LanguageLocation languageLocation = getLanguageLocation(languageLocationCode, language, circle, defaultForCircle);
        languageLocation.getDistrictSet().add(district);
    }

    private LanguageLocation getLanguageLocation(String languageLocationCode, Language language, Circle circle, Boolean defaultForCircle) {
        LanguageLocation languageLocation = languageLocationDataService.findByCode(languageLocationCode);
        if (null != languageLocation) {
            verify(Objects.equals(languageLocation.getLanguage().getName(), language.getName()),
                    "Language location exists, yet its language does not match the language supplied in the CSV record");
            verify(Objects.equals(languageLocation.getCircle().getName(), circle.getName()),
                    "Language location exists, yet its circle does not match the circle supplied in the CSV record");
            verify(Objects.equals(languageLocation.isDefaultForCircle(), defaultForCircle),
                    "Language location exists, yet its 'defaultForCircle' property does not match the value supplied in the CSV record");
        } else {
            verify(!defaultForCircle || null == circle.getDefaultLanguageLocation(),
                    "Language location with the code '%s' is marked as default for the circle, yet the default language location for this circle already exists", languageLocationCode);
            languageLocation = new LanguageLocation();
            languageLocation.setCode(languageLocationCode);
            languageLocation.setLanguage(language);
            languageLocation.setCircle(circle);
            languageLocation.setDefaultForCircle(defaultForCircle);
            languageLocation = languageLocationDataService.create(languageLocation);
        }
        return languageLocation;
    }

    private void verifyDistrictLanguageLocation(District district) {
        verify(null == district.getLanguageLocation(), "Language location for the '%s' district already specified", district.getName());
    }

    private void verify(boolean condition, String message, String... args) {
        if (!condition) {
            throw new CsvImportDataException(String.format(message, args));
        }
    }

    private Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(LANGUAGE_LOCATION_CODE, new GetString());
        mapping.put(LANGUAGE, new GetInstanceByString<Language>() {
            @Override
            public Language retrieve(String value) {
                Language language = languageDataService.findByName(value);
                if (null == language) {
                    language = languageDataService.create(new Language(value));
                }
                return language;
            }
        });
        mapping.put(CIRCLE, new GetInstanceByString<Circle>() {
            @Override
            public Circle retrieve(String value) {
                return circleDataService.findByName(value);
            }
        });
        mapping.put(STATE, new Optional(new GetInstanceByLong<State>() {
            @Override
            public State retrieve(Long value) {
                return stateDataService.findByCode(value);
            }
        }));
        mapping.put(DISTRICT, new Optional(new GetInstanceByLong<District>() {
            @Override
            public District retrieve(Long value) {
                return districtDataService.findByCode(value);
            }
        }));
        mapping.put(DEFAULT_FOR_CIRCLE, new GetBoolean());
        return mapping;
    }

    @Autowired
    public void setLanguageLocationDataService(LanguageLocationDataService languageLocationDataService) {
        this.languageLocationDataService = languageLocationDataService;
    }

    @Autowired
    public void setLanguageDataService(LanguageDataService languageDataService) {
        this.languageDataService = languageDataService;
    }

    @Autowired
    public void setCircleDataService(CircleDataService circleDataService) {
        this.circleDataService = circleDataService;
    }

    @Autowired
    public void setStateDataService(StateDataService stateDataService) {
        this.stateDataService = stateDataService;
    }

    @Autowired
    public void setDistrictDataService(DistrictDataService districtDataService) {
        this.districtDataService = districtDataService;
    }
}
