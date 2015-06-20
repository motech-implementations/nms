package org.motechproject.nms.region.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvInstanceImporter;
import org.motechproject.nms.csv.utils.GetInstanceByLong;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.csv.utils.Store;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.HealthBlockService;
import org.motechproject.nms.region.service.HealthFacilityService;
import org.motechproject.nms.region.service.TalukaService;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.ift.CellProcessor;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

public abstract class BaseLocationImportService<T> {

    public static final String STATE = "state";
    public static final String DISTRICT = "district";
    public static final String TALUKA = "taluka";
    public static final String HEALTH_BLOCK = "healthBlock";

    private Class<T> type;
    private MotechDataService<T> dataService;

    public BaseLocationImportService(Class<T> type, MotechDataService<T> dataService) {
        this.type = type;
        this.dataService = dataService;
    }

    @Transactional
    public void importData(Reader reader) throws IOException {
        CsvInstanceImporter<T> csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(getProcessorMapping())
                .setFieldNameMapping(getFieldNameMapping())
                .createAndOpen(reader, type);
        try {
            T instance;
            while (null != (instance = csvImporter.read())) {
                dataService.create(instance);
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(createErrorMessage(e.getConstraintViolations(),
                    csvImporter.getRowNumber()), e);
        } catch (IllegalStateException e) {
            throw new CsvImportDataException(createErrorMessage(e.getMessage(),
                csvImporter.getRowNumber()), e);
        }
    }

    protected CellProcessor mapState(final StateDataService stateDataService) {
        return new GetInstanceByLong<State>() {
            @Override
            public State retrieve(Long value) {
                return stateDataService.findByCode(value);
            }
        };
    }


    protected CellProcessor mapDistrict(final Store store, final DistrictService districtService) {
        return new GetInstanceByLong<District>() {
            @Override
            public District retrieve(Long value) {
                State state = (State) store.get(STATE);
                if (state == null) {
                    throw new IllegalStateException(String
                            .format("Unable to load District %s with a null state", value));
                }

                return districtService.findByStateAndCode(state, value);
            }
        };
    }

    protected CellProcessor mapTaluka(final Store store, final TalukaService talukaService) {
        return new GetInstanceByString<Taluka>() {
            @Override
            public Taluka retrieve(String value) {
                District district = (District) store.get(DISTRICT);
                if (district == null) {
                    throw new IllegalStateException(String
                            .format("Unable to load Taluka %s with a null district", value));
                }

                return talukaService.findByDistrictAndCode(district, value);
            }
        };
    }

    protected CellProcessor mapHealthBlock(final Store store, final HealthBlockService healthBlockService) {
        return new GetInstanceByLong<HealthBlock>() {
            @Override
            public HealthBlock retrieve(Long value) {
                Taluka taluka = (Taluka) store.get(TALUKA);
                if (taluka == null) {
                    throw new IllegalStateException(String
                            .format("Unable to load HealthBlock %s with a null taluka", value));
                }

                return healthBlockService.findByTalukaAndCode(taluka, value);
            }
        };
    }

    protected CellProcessor mapHealthFacility(final Store store,
                                              final HealthFacilityService healthFacilityService) {
        return new GetInstanceByLong<HealthFacility>() {
            @Override
            public HealthFacility retrieve(Long value) {
                HealthBlock healthBlock = (HealthBlock) store.get(HEALTH_BLOCK);
                if (healthBlock == null) {
                    throw new IllegalStateException(String
                            .format("Unable to load HealthFacility %s with a null HealthBlock", value));
                }

                return healthFacilityService.findByHealthBlockAndCode(healthBlock, value);
            }
        };
    }


    protected abstract Map<String, CellProcessor> getProcessorMapping();

    protected abstract Map<String, String> getFieldNameMapping();

    private String createErrorMessage(Set<ConstraintViolation<?>> violations, int rowNumber) {
        if (CollectionUtils.isNotEmpty(violations)) {
            return String.format("CSV instance error [row: %d]: validation failed for instance of type %s, violations: %s", rowNumber, type.getName(), ConstraintViolationUtils.toString(violations));
        } else {
            return String.format("CSV instance error [row: %d]", rowNumber);
        }
    }

    private String createErrorMessage(String message, int rowNumber) {
        return String.format("CSV instance error [row: %d]: Error loading entities in record for instance of type %s, message: %s", rowNumber, type.getName(), message);
    }
}
