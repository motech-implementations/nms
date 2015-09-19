package org.motechproject.nms.region.csv.impl;

import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.region.csv.CircleImportService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.service.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.ift.CellProcessor;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

@Service("circleImportService")
public class CircleImportServiceImpl implements CircleImportService {
    public static final String CIRCLE_NAME = "Circle";
    public static final String STATE_NAME = "State";

    private StateService stateService;
    private CircleDataService circleDataService;

    @Autowired
    public CircleImportServiceImpl(StateService stateService, CircleDataService circleDataService) {
        this.stateService = stateService;
        this.circleDataService = circleDataService;
    }

    @Override
    @Transactional
    public void importData(Reader reader) throws IOException {
        CsvMapImporter csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(getProcessorMapping())
                .createAndOpen(reader);
        Map<String, Object> record;
        while (null != (record = csvImporter.read())) {
            try {
                String circleName = (String) record.get(CIRCLE_NAME);
                if (circleName == null) {
                    throw new CsvImportDataException(String.format("CSV import error, circle missing on line %s",
                                                                   csvImporter.getRowNumber()));
                }

                String stateName = (String) record.get(STATE_NAME);
                if (stateName == null) {
                    throw new CsvImportDataException(String.format("CSV import error, state missing on line %s",
                            csvImporter.getRowNumber()));
                }

                State state = stateService.findByName(stateName);
                if (state == null) {
                    throw new CsvImportDataException(String.format("CSV import error, no state with name %s " +
                                    "on line %s", stateName, csvImporter.getRowNumber()));
                }

                Circle circle = circleDataService.findByName(circleName);
                if (circle == null) {
                    circle = new Circle(circleName);
                    circle = circleDataService.create(circle);
                }

                circle.getStates().add(state);
                circleDataService.update(circle);

                state.getCircles().add(circle);
                stateService.update(state);
            } catch (ConstraintViolationException e) {
                throw new CsvImportDataException(String.format("CSV import error, constraints violated: %s",
                        ConstraintViolationUtils.toString(e.getConstraintViolations())), e);
            }
        }
    }

    protected Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(CIRCLE_NAME, new GetString());
        mapping.put(STATE_NAME, new GetString());
        return mapping;
    }
}
