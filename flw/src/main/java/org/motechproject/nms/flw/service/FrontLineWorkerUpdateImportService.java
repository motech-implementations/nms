package org.motechproject.nms.flw.service;

import java.io.IOException;
import java.io.Reader;

public interface FrontLineWorkerUpdateImportService {

    void importLanguageData(Reader reader) throws IOException;

    void importMSISDNData(Reader reader) throws IOException;
}
