package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.nms.kilkari.service.MctsSubscriberImportService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;

/**
 * Implementation of the {@link MctsSubscriberImportService} interface.
 */
@Service("mctsSubscriberImportService")
public class MctsSubscriberImportServiceImpl implements MctsSubscriberImportService {

    @Override
    public void importMotherData(Reader reader) throws IOException {

    }

    @Override
    public void importChildData(Reader reader) throws IOException {

    }
}
