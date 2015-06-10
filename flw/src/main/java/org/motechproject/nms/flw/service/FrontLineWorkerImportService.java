package org.motechproject.nms.flw.service;

import java.io.IOException;
import java.io.Reader;

public interface FrontLineWorkerImportService {

    void importData(Reader reader) throws IOException;
}
