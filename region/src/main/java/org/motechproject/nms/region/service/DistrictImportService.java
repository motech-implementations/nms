package org.motechproject.nms.region.service;

import java.io.IOException;
import java.io.Reader;

public interface DistrictImportService {

    void importData(Reader reader) throws IOException;
}
